package com.example.cur_app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cur_app.data.remote.AuthApiService
import com.example.cur_app.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

/**
 * 用户认证数据仓库
 * 负责用户登录、注册、Token管理等功能
 */
@Singleton
class AuthRepository @Inject constructor(
    private val context: Context,
    private val authApiService: AuthApiService,
    private val chatRepositoryProvider: dagger.Lazy<ChatRepository>,
    private val preferencesRepository: PreferencesRepository
) {
    
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val NICKNAME_KEY = stringPreferencesKey("nickname")
        private val AVATAR_URL_KEY = stringPreferencesKey("avatar_url")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val TOKEN_EXPIRES_AT_KEY = longPreferencesKey("token_expires_at")
        private val LOGIN_TIME_KEY = longPreferencesKey("login_time")
        private val IS_TEST_MODE_KEY = booleanPreferencesKey("is_test_mode")
    }
    
    // ========== 认证状态 ==========
    
    /**
     * 检查是否已登录
     */
    val isLoggedIn: Flow<Boolean> = context.authDataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }
    
    /**
     * 检查是否为测试模式
     */
    val isTestMode: Flow<Boolean> = context.authDataStore.data.map { preferences ->
        preferences[IS_TEST_MODE_KEY] ?: false
    }
    
    /**
     * 获取当前用户信息
     */
    val currentUser: Flow<AuthUserInfo?> = context.authDataStore.data.map { preferences ->
        val isLoggedIn = preferences[IS_LOGGED_IN_KEY] ?: false
        if (isLoggedIn) {
            AuthUserInfo(
                userId = preferences[USER_ID_KEY] ?: "",
                username = preferences[USERNAME_KEY] ?: "",
                email = preferences[EMAIL_KEY] ?: "",
                nickname = preferences[NICKNAME_KEY] ?: "",
                avatarUrl = preferences[AVATAR_URL_KEY]
            )
        } else {
            null
        }
    }
    
    /**
     * 获取访问令牌
     */
    suspend fun getAccessToken(): String? {
        return context.authDataStore.data.first()[ACCESS_TOKEN_KEY]
    }
    
    /**
     * 获取刷新令牌
     */
    suspend fun getRefreshToken(): String? {
        return context.authDataStore.data.first()[REFRESH_TOKEN_KEY]
    }
    
    /**
     * 检查Token是否过期
     */
    suspend fun isTokenExpired(): Boolean {
        val expiresAt = context.authDataStore.data.first()[TOKEN_EXPIRES_AT_KEY] ?: 0L
        return System.currentTimeMillis() >= expiresAt
    }
    
    // ========== 用户认证 ==========
    
    /**
     * 用户登录
     */
    suspend fun login(
        username: String,
        password: String,
        deviceInfo: DeviceInfo? = null
    ): Result<LoginData> {
        return try {
            val request = LoginRequest(
                username = username,
                password = password,
                deviceInfo = deviceInfo
            )
            
            val response = authApiService.login(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val loginData = response.body()?.data
                if (loginData != null) {
                    // 保存登录信息
                    saveAuthInfo(loginData)
                    Result.success(loginData)
                } else {
                    Result.failure(Exception("登录数据为空"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "登录失败"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 用户注册
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        nickname: String,
        phone: String? = null,
        verificationCode: String? = null,
        inviteCode: String? = null,
        deviceInfo: DeviceInfo? = null
    ): Result<RegisterData> {
        return try {
            val request = RegisterRequest(
                username = username,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                nickname = nickname,
                phone = phone,
                verificationCode = verificationCode,
                inviteCode = inviteCode,
                deviceInfo = deviceInfo
            )
            
            val response = authApiService.register(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val registerData = response.body()?.data
                if (registerData != null) {
                    // 如果注册成功且提供了token，保存登录信息
                    if (registerData.token != null) {
                        val loginData = LoginData(
                            user = registerData.user,
                            token = registerData.token,
                            permissions = emptyList(),
                            settings = null
                        )
                        saveAuthInfo(loginData)
                    }
                    Result.success(registerData)
                } else {
                    Result.failure(Exception("注册数据为空"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "注册失败"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 刷新Token
     */
    suspend fun refreshToken(): Result<TokenInfo> {
        return try {
            val refreshToken = getRefreshToken()
            if (refreshToken.isNullOrEmpty()) {
                return Result.failure(Exception("刷新令牌为空"))
            }
            
            val request = RefreshTokenRequest(refreshToken = refreshToken)
            val response = authApiService.refreshToken(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val tokenInfo = response.body()?.data
                if (tokenInfo != null) {
                    // 更新Token信息
                    updateTokenInfo(tokenInfo)
                    Result.success(tokenInfo)
                } else {
                    Result.failure(Exception("Token数据为空"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "刷新令牌失败"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 用户登出
     */
    suspend fun logout(): Result<Unit> {
        return try {
            val token = getAccessToken()
            if (!token.isNullOrEmpty()) {
                // 调用服务器登出接口
                val response = authApiService.logout("Bearer $token")
                // 忽略服务器响应，直接清除本地数据
            }
            
            // 清除本地认证信息
            clearAuthInfo()
            Result.success(Unit)
        } catch (e: Exception) {
            // 即使网络请求失败，也要清除本地数据
            clearAuthInfo()
            Result.success(Unit)
        }
    }
    
    /**
     * 发送验证码
     */
    suspend fun sendVerificationCode(
        target: String,
        type: String, // email, sms
        purpose: String // register, login, reset, verify
    ): Result<Unit> {
        return try {
            val request = SendCodeRequest(
                target = target,
                type = type,
                purpose = purpose
            )
            
            val response = authApiService.sendVerificationCode(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "发送验证码失败"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 验证邮箱
     */
    suspend fun verifyEmail(
        email: String,
        code: String,
        type: String = "register"
    ): Result<Unit> {
        return try {
            val request = VerifyEmailRequest(
                email = email,
                code = code,
                type = type
            )
            
            val response = authApiService.verifyEmail(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "邮箱验证失败"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 重置密码
     */
    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        return try {
            val request = ResetPasswordRequest(
                email = email,
                code = code,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )
            
            val response = authApiService.resetPassword(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "重置密码失败"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 测试模式 ==========
    
    /**
     * 启用测试模式（使用test用户登录）
     */
    suspend fun enableTestMode(): Result<LoginData> {
        return try {
            // 使用预设的测试用户账号进行登录
            val result = login(
                username = "test",
                password = "123456", 
                deviceInfo = DeviceInfo(
                    deviceId = "test_device",
                    deviceName = "Test Device",
                    platform = "Android",
                    osVersion = android.os.Build.VERSION.RELEASE,
                    appVersion = "1.0.0",
                    manufacturer = "Test",
                    model = "TestMode"
                )
            )
            
            result.fold(
                onSuccess = { loginData ->
                    // 标记为测试模式
                    context.authDataStore.edit { preferences ->
                        preferences[IS_TEST_MODE_KEY] = true
                    }
                    Result.success(loginData)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 禁用测试模式
     */
    suspend fun disableTestMode() {
        context.authDataStore.edit { preferences ->
            preferences[IS_TEST_MODE_KEY] = false
        }
        clearAuthInfo()
    }
    
    // ========== 私有方法 ==========
    
    /**
     * 保存认证信息
     */
    private suspend fun saveAuthInfo(loginData: LoginData) {
        context.authDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = loginData.token.accessToken
            preferences[REFRESH_TOKEN_KEY] = loginData.token.refreshToken
            preferences[USER_ID_KEY] = loginData.user.userId
            preferences[USERNAME_KEY] = loginData.user.username
            preferences[EMAIL_KEY] = loginData.user.email
            preferences[NICKNAME_KEY] = loginData.user.nickname
            preferences[AVATAR_URL_KEY] = loginData.user.avatarUrl ?: ""
            preferences[IS_LOGGED_IN_KEY] = true
            preferences[TOKEN_EXPIRES_AT_KEY] = System.currentTimeMillis() + (loginData.token.expiresIn * 1000)
            preferences[LOGIN_TIME_KEY] = System.currentTimeMillis()
            preferences[IS_TEST_MODE_KEY] = false
        }
        
        // 更新PreferencesRepository中的用户信息以同步UI显示
        val userProfile = UserProfile(
            nickname = loginData.user.nickname,
            signature = "用AI助手，让好习惯成为生活日常",
            avatarType = "emoji",
            avatarValue = "😊",
            userId = loginData.user.userId
        )
        preferencesRepository.saveUserProfile(userProfile)
        
        // 登录成功后自动连接Socket.IO进行实时聊天
        try {
            chatRepositoryProvider.get().connectSocket(loginData.token.accessToken)
        } catch (e: Exception) {
            // Socket连接失败不影响登录流程
            android.util.Log.w("AuthRepository", "Socket connection failed: ${e.message}")
        }
    }
    
    /**
     * 更新Token信息
     */
    private suspend fun updateTokenInfo(tokenInfo: TokenInfo) {
        context.authDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = tokenInfo.accessToken
            preferences[REFRESH_TOKEN_KEY] = tokenInfo.refreshToken
            preferences[TOKEN_EXPIRES_AT_KEY] = System.currentTimeMillis() + (tokenInfo.expiresIn * 1000)
        }
    }
    
    /**
     * 清除认证信息
     */
    private suspend fun clearAuthInfo() {
        context.authDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USERNAME_KEY)
            preferences.remove(EMAIL_KEY)
            preferences.remove(NICKNAME_KEY)
            preferences.remove(AVATAR_URL_KEY)
            preferences.remove(TOKEN_EXPIRES_AT_KEY)
            preferences.remove(LOGIN_TIME_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
            preferences[IS_TEST_MODE_KEY] = false
        }
        
        // 登出时断开Socket.IO连接
        try {
            chatRepositoryProvider.get().disconnectSocket()
        } catch (e: Exception) {
            // Socket断开失败不影响登出流程
            android.util.Log.w("AuthRepository", "Socket disconnect failed: ${e.message}")
        }
    }
}

/**
 * 认证用户信息
 */
data class AuthUserInfo(
    val userId: String,
    val username: String,
    val email: String,
    val nickname: String,
    val avatarUrl: String? = null
)