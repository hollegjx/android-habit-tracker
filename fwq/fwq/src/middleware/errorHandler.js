function errorHandler(err, req, res, next) {
  console.error('错误详情:', err);

  // Joi 验证错误
  if (err.isJoi) {
    return res.status(400).json({
      success: false,
      message: '请求参数验证失败',
      errors: err.details.map(detail => ({
        field: detail.path.join('.'),
        message: detail.message
      }))
    });
  }

  // 数据库唯一约束错误
  if (err.code === '23505') {
    return res.status(400).json({
      success: false,
      message: '数据已存在，请检查输入'
    });
  }

  // 数据库外键约束错误
  if (err.code === '23503') {
    return res.status(400).json({
      success: false,
      message: '关联数据不存在'
    });
  }

  // 文件上传错误
  if (err.code === 'LIMIT_FILE_SIZE') {
    return res.status(400).json({
      success: false,
      message: '文件大小超出限制'
    });
  }

  if (err.code === 'LIMIT_UNEXPECTED_FILE') {
    return res.status(400).json({
      success: false,
      message: '上传的文件类型不支持'
    });
  }

  // 默认服务器错误
  res.status(err.status || 500).json({
    success: false,
    message: err.message || '服务器内部错误',
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
  });
}

module.exports = errorHandler;