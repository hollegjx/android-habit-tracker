const express = require('express');
const { validate } = require('../middleware/validation');
const { authenticate, requireAdmin } = require('../middleware/auth');
const { aiCharacterSchema } = require('../utils/validation');
const {
  getAICharacters,
  getAICharacter,
  createAICharacter,
  updateAICharacter,
  deleteAICharacter,
  getAllAICharacters
} = require('../controllers/aiCharacterController');

const router = express.Router();

// 获取所有可用的AI角色（无需认证）
router.get('/', getAICharacters);

// 获取单个AI角色详情
router.get('/:characterId', authenticate, getAICharacter);

// 以下路由需要管理员权限
router.use(authenticate, requireAdmin);

// 获取所有AI角色（管理员专用）
router.get('/admin/all', getAllAICharacters);

// 获取单个AI角色详情（管理员专用，包含敏感信息）
router.get('/admin/:characterId', getAICharacter);

// 创建AI角色
router.post('/', validate(aiCharacterSchema), createAICharacter);

// 更新AI角色
router.put('/:characterId', updateAICharacter);

// 删除AI角色
router.delete('/:characterId', deleteAICharacter);

module.exports = router;