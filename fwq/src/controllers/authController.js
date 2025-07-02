const db = require('../utils/database');
const { 
  generateUID, 
  hashPassword, 
  verifyPassword, 
  generateAccessToken, 
  generateRefreshToken,
  verifyRefreshToken,
  generateVerificationCode 
} = require('../utils/auth');

// 用户注册
async function register(req, res) {
  try {
    const { username, email, password, nickname, phone, verificationCode } = req.body;
    
    // 检查用户名是否已存在
    const existingUser = await db('users')
      .where('username', username)
      .orWhere('email', email)
      .first();
    
    if (existingUser) {
      if (existingUser.username === username) {
        return res.status(400).json({
          success: false,
          message: '用户名已被使用'
        });
      }
      if (existingUser.email === email) {
        return res.status(400).json({
          success: false,
          message: '邮箱已被注册'
        });
      }
    }

    // 生成唯一UID
    let uid;
    let isUidUnique = false;
    while (!isUidUnique) {
      uid = generateUID();
      const uidExists = await db('users').where('uid', uid).first();
      if (!uidExists) {
        isUidUnique = true;
      }
    }

    // 哈希密码
    const passwordHash = await hashPassword(password);

    // 创建用户
    const [user] = await db('users')
      .insert({
        uid,
        username,
        email,
        password_hash: passwordHash,
        nickname,
        phone: phone || null,
        email_verified: !verificationCode, // 如果有验证码则视为已验证
        created_at: new Date(),
        updated_at: new Date()
      })
      .returning(['id', 'uid', 'username', 'email', 'nickname', 'phone', 'avatar_url', 'role', 'created_at']);

    // 生成令牌
    const accessToken = generateAccessToken({ 
      userId: user.id, 
      username: user.username,
      role: user.role 
    });
    const refreshToken = generateRefreshToken({ userId: user.id });

    // 保存刷新令牌
    await db('refresh_tokens').insert({
      user_id: user.id,
      token: refreshToken,
      device_info: JSON.stringify(req.body.deviceInfo || {}),
      expires_at: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7天
      created_at: new Date()
    });

    res.status(201).json({
      success: true,
      message: '注册成功',
      data: {
        user: {
          userId: user.id.toString(),
          uid: user.uid,
          username: user.username,
          email: user.email,
          nickname: user.nickname,
          phone: user.phone,
          avatarUrl: user.avatar_url,
          role: user.role,
          joinedAt: new Date(user.created_at).getTime(),
          updatedAt: new Date(user.updated_at || user.created_at).getTime()
        },
        token: {
          accessToken,
          refreshToken,
          tokenType: 'Bearer',
          expiresIn: 86400
        },
        needVerification: !!verificationCode
      }
    });

  } catch (error) {
    console.error('注册错误:', error);
    res.status(500).json({
      success: false,
      message: '注册失败，请重试'
    });
  }
}

// 用户登录
async function login(req, res) {
  try {
    const { username, password, deviceInfo } = req.body;

    // 查找用户（支持用户名或邮箱登录）
    const user = await db('users')
      .where('username', username)
      .orWhere('email', username)
      .first();

    if (!user) {
      return res.status(401).json({
        success: false,
        message: '用户名或密码错误'
      });
    }

    if (!user.is_active) {
      return res.status(401).json({
        success: false,
        message: '账号已被禁用'
      });
    }

    // 验证密码
    const isPasswordValid = await verifyPassword(password, user.password_hash);
    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: '用户名或密码错误'
      });
    }

    // 更新最后登录时间
    await db('users')
      .where('id', user.id)
      .update({ last_login_at: new Date() });

    // 生成令牌
    const accessToken = generateAccessToken({ 
      userId: user.id, 
      username: user.username,
      role: user.role 
    });
    const refreshToken = generateRefreshToken({ userId: user.id });

    // 保存刷新令牌
    await db('refresh_tokens').insert({
      user_id: user.id,
      token: refreshToken,
      device_info: JSON.stringify(deviceInfo || {}),
      expires_at: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
      created_at: new Date()
    });

    res.json({
      success: true,
      message: '登录成功',
      data: {
        user: {
          userId: user.id.toString(),
          uid: user.uid,
          username: user.username,
          email: user.email,
          nickname: user.nickname,
          phone: user.phone,
          avatarUrl: user.avatar_url,
          role: user.role,
          joinedAt: new Date(user.created_at).getTime(),
          updatedAt: new Date(user.updated_at || user.created_at).getTime()
        },
        token: {
          accessToken,
          refreshToken,
          tokenType: 'Bearer',
          expiresIn: 86400
        }
      }
    });

  } catch (error) {
    console.error('登录错误:', error);
    res.status(500).json({
      success: false,
      message: '登录失败，请重试'
    });
  }
}

// 刷新令牌
async function refreshToken(req, res) {
  try {
    const { refreshToken } = req.body;

    // 验证刷新令牌
    const decoded = verifyRefreshToken(refreshToken);
    
    // 检查数据库中的令牌
    const tokenRecord = await db('refresh_tokens')
      .where('token', refreshToken)
      .where('user_id', decoded.userId)
      .where('expires_at', '>', new Date())
      .first();

    if (!tokenRecord) {
      return res.status(401).json({
        success: false,
        message: '刷新令牌无效或已过期'
      });
    }

    // 获取用户信息
    const user = await db('users').where('id', decoded.userId).first();
    if (!user || !user.is_active) {
      return res.status(401).json({
        success: false,
        message: '用户不存在或已被禁用'
      });
    }

    // 生成新的访问令牌
    const newAccessToken = generateAccessToken({
      userId: user.id,
      username: user.username,
      role: user.role
    });

    res.json({
      success: true,
      data: {
        accessToken: newAccessToken,
        tokenType: 'Bearer',
        expiresIn: 86400
      }
    });

  } catch (error) {
    console.error('刷新令牌错误:', error);
    res.status(401).json({
      success: false,
      message: '刷新令牌失败'
    });
  }
}

// 登出
async function logout(req, res) {
  try {
    const { refreshToken } = req.body;
    const userId = req.user.userId;

    // 删除刷新令牌
    if (refreshToken) {
      await db('refresh_tokens')
        .where('token', refreshToken)
        .where('user_id', userId)
        .del();
    } else {
      // 删除用户所有刷新令牌
      await db('refresh_tokens')
        .where('user_id', userId)
        .del();
    }

    res.json({
      success: true,
      message: '登出成功'
    });

  } catch (error) {
    console.error('登出错误:', error);
    res.status(500).json({
      success: false,
      message: '登出失败'
    });
  }
}

// 发送验证码
async function sendVerificationCode(req, res) {
  try {
    const { target, type, purpose } = req.body;

    // 生成验证码
    const code = generateVerificationCode(6);
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000); // 10分钟过期

    // 保存验证码
    await db('verification_codes').insert({
      target,
      type,
      purpose,
      code,
      expires_at: expiresAt,
      created_at: new Date()
    });

    // TODO: 实际发送邮件或短信
    console.log(`验证码: ${code} 发送到 ${target}`);

    res.json({
      success: true,
      message: `验证码已发送到${type === 'email' ? '邮箱' : '手机号'}`
    });

  } catch (error) {
    console.error('发送验证码错误:', error);
    res.status(500).json({
      success: false,
      message: '发送验证码失败'
    });
  }
}

// 重置密码
async function resetPassword(req, res) {
  try {
    const { email, code, newPassword } = req.body;

    // 验证验证码
    const verification = await db('verification_codes')
      .where('target', email)
      .where('type', 'email')
      .where('purpose', 'reset')
      .where('code', code)
      .where('is_used', false)
      .where('expires_at', '>', new Date())
      .first();

    if (!verification) {
      return res.status(400).json({
        success: false,
        message: '验证码无效或已过期'
      });
    }

    // 查找用户
    const user = await db('users').where('email', email).first();
    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    // 更新密码
    const passwordHash = await hashPassword(newPassword);
    await db('users')
      .where('id', user.id)
      .update({ 
        password_hash: passwordHash,
        updated_at: new Date()
      });

    // 标记验证码为已使用
    await db('verification_codes')
      .where('id', verification.id)
      .update({ is_used: true });

    // 删除所有刷新令牌
    await db('refresh_tokens').where('user_id', user.id).del();

    res.json({
      success: true,
      message: '密码重置成功'
    });

  } catch (error) {
    console.error('重置密码错误:', error);
    res.status(500).json({
      success: false,
      message: '重置密码失败'
    });
  }
}

module.exports = {
  register,
  login,
  refreshToken,
  logout,
  sendVerificationCode,
  resetPassword
};