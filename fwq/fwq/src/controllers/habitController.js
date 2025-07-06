const db = require('../utils/database');
const { generateUID } = require('../utils/auth');

// 获取用户的所有习惯
async function getUserHabits(req, res) {
  try {
    const userId = req.user.userId;
    const { isActive = true } = req.query;

    const habits = await db('habits')
      .where('user_id', userId)
      .where('is_active', isActive)
      .orderBy('created_at', 'desc');

    // 获取每个习惯的最新统计信息
    const habitsWithStats = await Promise.all(habits.map(async (habit) => {
      const latestStats = await db('habit_statistics')
        .where('habit_id', habit.id)
        .orderBy('stat_date', 'desc')
        .first();

      return {
        ...habit,
        latest_stats: latestStats
      };
    }));

    res.json({
      success: true,
      data: habitsWithStats
    });
  } catch (error) {
    console.error('获取用户习惯失败:', error);
    res.status(500).json({
      success: false,
      message: '获取习惯列表失败'
    });
  }
}

// 创建新习惯
async function createHabit(req, res) {
  try {
    const userId = req.user.userId;
    const {
      name,
      description,
      category = 'general',
      frequency = 'daily',
      targetValue = 1,
      unit = 'times',
      reminderTime,
      metadata = {}
    } = req.body;

    const habitId = generateUID();

    const [habit] = await db('habits')
      .insert({
        habit_id: habitId,
        user_id: userId,
        name,
        description,
        category,
        frequency,
        target_value: targetValue,
        unit,
        reminder_time: reminderTime,
        metadata: JSON.stringify(metadata)
      })
      .returning('*');

    res.status(201).json({
      success: true,
      message: '习惯创建成功',
      data: habit
    });
  } catch (error) {
    console.error('创建习惯失败:', error);
    res.status(500).json({
      success: false,
      message: '创建习惯失败'
    });
  }
}

// 更新习惯
async function updateHabit(req, res) {
  try {
    const userId = req.user.userId;
    const { habitId } = req.params;
    const updateData = req.body;

    // 验证习惯所有权
    const habit = await db('habits')
      .where('habit_id', habitId)
      .where('user_id', userId)
      .first();

    if (!habit) {
      return res.status(404).json({
        success: false,
        message: '习惯不存在'
      });
    }

    // 准备更新数据
    const allowedFields = [
      'name', 'description', 'category', 'frequency', 
      'target_value', 'unit', 'reminder_time', 'is_active', 'metadata'
    ];
    
    const filteredUpdateData = {};
    Object.keys(updateData).forEach(key => {
      if (allowedFields.includes(key)) {
        filteredUpdateData[key] = updateData[key];
      }
    });

    // 如果有metadata，需要序列化
    if (filteredUpdateData.metadata) {
      filteredUpdateData.metadata = JSON.stringify(filteredUpdateData.metadata);
    }

    const [updatedHabit] = await db('habits')
      .where('habit_id', habitId)
      .where('user_id', userId)
      .update({
        ...filteredUpdateData,
        updated_at: new Date()
      })
      .returning('*');

    res.json({
      success: true,
      message: '习惯更新成功',
      data: updatedHabit
    });
  } catch (error) {
    console.error('更新习惯失败:', error);
    res.status(500).json({
      success: false,
      message: '更新习惯失败'
    });
  }
}

// 删除习惯
async function deleteHabit(req, res) {
  try {
    const userId = req.user.userId;
    const { habitId } = req.params;

    // 验证习惯所有权
    const habit = await db('habits')
      .where('habit_id', habitId)
      .where('user_id', userId)
      .first();

    if (!habit) {
      return res.status(404).json({
        success: false,
        message: '习惯不存在'
      });
    }

    // 软删除：设置为非活跃状态
    await db('habits')
      .where('habit_id', habitId)
      .where('user_id', userId)
      .update({
        is_active: false,
        updated_at: new Date()
      });

    res.json({
      success: true,
      message: '习惯删除成功'
    });
  } catch (error) {
    console.error('删除习惯失败:', error);
    res.status(500).json({
      success: false,
      message: '删除习惯失败'
    });
  }
}

// 记录习惯完成
async function recordHabitCompletion(req, res) {
  try {
    const userId = req.user.userId;
    const { habitId } = req.params;
    const {
      value = 1,
      notes,
      completedAt = new Date().toISOString()
    } = req.body;

    // 验证习惯所有权
    const habit = await db('habits')
      .where('habit_id', habitId)
      .where('user_id', userId)
      .where('is_active', true)
      .first();

    if (!habit) {
      return res.status(404).json({
        success: false,
        message: '习惯不存在或已被删除'
      });
    }

    const completionId = generateUID();
    const completedAtDate = new Date(completedAt);
    const statDate = completedAtDate.toISOString().split('T')[0]; // YYYY-MM-DD

    // 开始事务
    await db.transaction(async (trx) => {
      // 记录完成
      await trx('habit_completions').insert({
        completion_id: completionId,
        habit_id: habit.id,
        user_id: userId,
        value,
        notes,
        completed_at: completedAtDate
      });

      // 更新或创建统计记录
      const existingStats = await trx('habit_statistics')
        .where('habit_id', habit.id)
        .where('stat_date', statDate)
        .first();

      if (existingStats) {
        // 更新现有统计
        const newCompletedValue = parseFloat(existingStats.completed_value) + parseFloat(value);
        const isCompleted = newCompletedValue >= habit.target_value;

        await trx('habit_statistics')
          .where('id', existingStats.id)
          .update({
            completed_value: newCompletedValue,
            is_completed: isCompleted,
            updated_at: new Date()
          });
      } else {
        // 创建新统计记录
        const isCompleted = parseFloat(value) >= habit.target_value;
        
        // 计算连续完成天数
        const streakCount = await calculateStreakCount(trx, habit.id, statDate);

        await trx('habit_statistics').insert({
          habit_id: habit.id,
          user_id: userId,
          stat_date: statDate,
          completed_value: value,
          is_completed: isCompleted,
          streak_count: streakCount
        });
      }
    });

    res.json({
      success: true,
      message: '习惯完成记录成功',
      data: {
        completion_id: completionId,
        completed_at: completedAtDate
      }
    });
  } catch (error) {
    console.error('记录习惯完成失败:', error);
    res.status(500).json({
      success: false,
      message: '记录习惯完成失败'
    });
  }
}

// 获取习惯统计信息
async function getHabitStatistics(req, res) {
  try {
    const userId = req.user.userId;
    const { habitId } = req.params;
    const { 
      startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 30天前
      endDate = new Date().toISOString().split('T')[0] // 今天
    } = req.query;

    // 验证习惯所有权
    const habit = await db('habits')
      .where('habit_id', habitId)
      .where('user_id', userId)
      .first();

    if (!habit) {
      return res.status(404).json({
        success: false,
        message: '习惯不存在'
      });
    }

    // 获取统计数据
    const statistics = await db('habit_statistics')
      .where('habit_id', habit.id)
      .where('stat_date', '>=', startDate)
      .where('stat_date', '<=', endDate)
      .orderBy('stat_date', 'asc');

    // 计算总体统计
    const totalDays = statistics.length;
    const completedDays = statistics.filter(stat => stat.is_completed).length;
    const completionRate = totalDays > 0 ? (completedDays / totalDays * 100).toFixed(1) : 0;
    const currentStreak = statistics.length > 0 ? statistics[statistics.length - 1].streak_count : 0;

    res.json({
      success: true,
      data: {
        habit_info: {
          habit_id: habit.habit_id,
          name: habit.name,
          target_value: habit.target_value,
          unit: habit.unit
        },
        summary: {
          total_days: totalDays,
          completed_days: completedDays,
          completion_rate: parseFloat(completionRate),
          current_streak: currentStreak
        },
        daily_stats: statistics
      }
    });
  } catch (error) {
    console.error('获取习惯统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取习惯统计失败'
    });
  }
}

// 计算连续完成天数的辅助函数
async function calculateStreakCount(trx, habitId, currentDate) {
  const stats = await trx('habit_statistics')
    .where('habit_id', habitId)
    .where('stat_date', '<', currentDate)
    .where('is_completed', true)
    .orderBy('stat_date', 'desc')
    .limit(365); // 最多查询一年的数据

  let streakCount = 1; // 包含当前日期
  const currentDateObj = new Date(currentDate);

  for (let i = 0; i < stats.length; i++) {
    const statDate = new Date(stats[i].stat_date);
    const expectedDate = new Date(currentDateObj);
    expectedDate.setDate(currentDateObj.getDate() - (i + 1));

    if (statDate.getTime() === expectedDate.getTime()) {
      streakCount++;
    } else {
      break; // 连续性中断
    }
  }

  return streakCount;
}

module.exports = {
  getUserHabits,
  createHabit,
  updateHabit,
  deleteHabit,
  recordHabitCompletion,
  getHabitStatistics
};