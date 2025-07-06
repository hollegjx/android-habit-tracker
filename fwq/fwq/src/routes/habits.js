const express = require('express');
const { authenticate } = require('../middleware/auth');
const { validate } = require('../middleware/validation');
const {
  getUserHabits,
  createHabit,
  updateHabit,
  deleteHabit,
  recordHabitCompletion,
  getHabitStatistics
} = require('../controllers/habitController');

const router = express.Router();

// 所有习惯路由都需要认证
router.use(authenticate);

// 获取用户的所有习惯
router.get('/', getUserHabits);

// 创建新习惯
router.post('/', createHabit);

// 更新习惯
router.put('/:habitId', updateHabit);

// 删除习惯
router.delete('/:habitId', deleteHabit);

// 记录习惯完成
router.post('/:habitId/complete', recordHabitCompletion);

// 获取习惯统计信息
router.get('/:habitId/statistics', getHabitStatistics);

module.exports = router;