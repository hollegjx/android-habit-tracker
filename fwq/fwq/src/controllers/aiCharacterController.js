const db = require('../utils/database');
const { generateUUID } = require('../utils/auth');

// 获取所有可用的AI角色
async function getAICharacters(req, res) {
  try {
    const characters = await db('ai_characters')
      .select('character_id', 'name', 'description', 'avatar_url', 'personality')
      .where('is_active', true)
      .orderBy('created_at', 'desc');

    res.json({
      success: true,
      data: characters
    });
  } catch (error) {
    console.error('获取AI角色错误:', error);
    res.status(500).json({
      success: false,
      message: '获取AI角色失败'
    });
  }
}

// 获取单个AI角色详情
async function getAICharacter(req, res) {
  try {
    const { characterId } = req.params;

    const character = await db('ai_characters')
      .where('character_id', characterId)
      .where('is_active', true)
      .first();

    if (!character) {
      return res.status(404).json({
        success: false,
        message: 'AI角色不存在'
      });
    }

    // 如果不是管理员，不返回敏感信息
    if (req.user.role !== 'admin') {
      delete character.system_prompt;
      delete character.model_config;
      delete character.created_by;
    }

    res.json({
      success: true,
      data: character
    });
  } catch (error) {
    console.error('获取AI角色详情错误:', error);
    res.status(500).json({
      success: false,
      message: '获取AI角色详情失败'
    });
  }
}

// 创建AI角色（仅管理员）
async function createAICharacter(req, res) {
  try {
    const {
      name,
      description,
      personality,
      systemPrompt,
      model = process.env.DEFAULT_AI_MODEL || 'gpt-4.1',
      modelConfig = {}
    } = req.body;

    const characterId = generateUUID();

    const [character] = await db('ai_characters')
      .insert({
        character_id: characterId,
        name,
        description,
        personality,
        system_prompt: systemPrompt,
        model,
        model_config: JSON.stringify(modelConfig),
        is_active: true,
        created_by: req.user.userId,
        created_at: new Date()
      })
      .returning('*');

    res.status(201).json({
      success: true,
      message: 'AI角色创建成功',
      data: character
    });
  } catch (error) {
    console.error('创建AI角色错误:', error);
    res.status(500).json({
      success: false,
      message: '创建AI角色失败'
    });
  }
}

// 更新AI角色（仅管理员）
async function updateAICharacter(req, res) {
  try {
    const { characterId } = req.params;
    const {
      name,
      description,
      personality,
      systemPrompt,
      model,
      modelConfig,
      isActive
    } = req.body;

    const character = await db('ai_characters')
      .where('character_id', characterId)
      .first();

    if (!character) {
      return res.status(404).json({
        success: false,
        message: 'AI角色不存在'
      });
    }

    const updateData = {
      updated_at: new Date()
    };

    if (name !== undefined) updateData.name = name;
    if (description !== undefined) updateData.description = description;
    if (personality !== undefined) updateData.personality = personality;
    if (systemPrompt !== undefined) updateData.system_prompt = systemPrompt;
    if (model !== undefined) updateData.model = model;
    if (modelConfig !== undefined) updateData.model_config = JSON.stringify(modelConfig);
    if (isActive !== undefined) updateData.is_active = isActive;

    await db('ai_characters')
      .where('character_id', characterId)
      .update(updateData);

    const updatedCharacter = await db('ai_characters')
      .where('character_id', characterId)
      .first();

    res.json({
      success: true,
      message: 'AI角色更新成功',
      data: updatedCharacter
    });
  } catch (error) {
    console.error('更新AI角色错误:', error);
    res.status(500).json({
      success: false,
      message: '更新AI角色失败'
    });
  }
}

// 删除AI角色（仅管理员）
async function deleteAICharacter(req, res) {
  try {
    const { characterId } = req.params;

    const character = await db('ai_characters')
      .where('character_id', characterId)
      .first();

    if (!character) {
      return res.status(404).json({
        success: false,
        message: 'AI角色不存在'
      });
    }

    // 软删除：设置为不活跃
    await db('ai_characters')
      .where('character_id', characterId)
      .update({
        is_active: false,
        updated_at: new Date()
      });

    res.json({
      success: true,
      message: 'AI角色删除成功'
    });
  } catch (error) {
    console.error('删除AI角色错误:', error);
    res.status(500).json({
      success: false,
      message: '删除AI角色失败'
    });
  }
}

// 获取所有AI角色（管理员专用，包括未激活的）
async function getAllAICharacters(req, res) {
  try {
    const characters = await db('ai_characters')
      .select('*')
      .orderBy('created_at', 'desc');

    res.json({
      success: true,
      data: characters
    });
  } catch (error) {
    console.error('获取所有AI角色错误:', error);
    res.status(500).json({
      success: false,
      message: '获取AI角色失败'
    });
  }
}

module.exports = {
  getAICharacters,
  getAICharacter,
  createAICharacter,
  updateAICharacter,
  deleteAICharacter,
  getAllAICharacters
};