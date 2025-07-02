const bcrypt = require('bcryptjs');

exports.seed = async function(knex) {
  // 删除现有数据
  await knex('users').del();

  // 生成管理员UID
  function generateUID() {
    return Math.floor(10000000000 + Math.random() * 90000000000).toString();
  }

  // 插入管理员用户
  const adminPassword = await bcrypt.hash(process.env.ADMIN_PASSWORD || 'admin123456', 12);
  
  await knex('users').insert({
    uid: generateUID(),
    username: 'admin',
    email: process.env.ADMIN_EMAIL || 'admin@example.com',
    password_hash: adminPassword,
    nickname: '系统管理员',
    role: 'admin',
    is_active: true,
    email_verified: true,
    created_at: knex.fn.now(),
    updated_at: knex.fn.now()
  });

  console.log('✅ 管理员账号创建成功');
  console.log(`📧 邮箱: ${process.env.ADMIN_EMAIL || 'admin@example.com'}`);
  console.log(`🔑 密码: ${process.env.ADMIN_PASSWORD || 'admin123456'}`);
};