package com.example.cur_app.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.data.repository.AuthRepository
import com.example.cur_app.data.remote.dto.DeviceInfo
import com.example.cur_app.data.database.DefaultDataInitializer
import com.example.cur_app.data.database.HabitTrackerDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户认证ViewModel
 * 处理登录、注册、密码重置等认证相关功能
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val database: HabitTrackerDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // 监听认证状态变化
        viewModelScope.launch {
            combine(
                authRepository.isLoggedIn,
                authRepository.isTestMode,
                authRepository.currentUser
            ) { isLoggedIn, isTestMode, currentUser ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = isLoggedIn,
                    isTestMode = isTestMode,
                    currentUser = currentUser
                )
            }
        }
    }

    // ========== 登录相关 ==========

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun toggleRememberLogin() {
        _uiState.value = _uiState.value.copy(
            rememberLogin = !_uiState.value.rememberLogin
        )
    }

    fun login() {
        val currentState = _uiState.value
        
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(error = "请输入用户名和密码")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            try {
                val deviceInfo = getDeviceInfo()
                val result = authRepository.login(
                    username = currentState.username.trim(),
                    password = currentState.password,
                    deviceInfo = deviceInfo
                )

                result.fold(
                    onSuccess = { loginData ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            successMessage = "登录成功！欢迎回来"
                        )
                        
                        // 检查并初始化首次登录的默认数据
                        initializeFirstLoginData()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "登录失败，请重试"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "网络连接失败，请检查网络设置"
                )
            }
        }
    }

    // ========== 注册相关 ==========

    fun updateRegisterUsername(username: String) {
        _uiState.value = _uiState.value.copy(registerUsername = username)
    }

    fun updateRegisterEmail(email: String) {
        _uiState.value = _uiState.value.copy(registerEmail = email)
    }

    fun updateRegisterPassword(password: String) {
        _uiState.value = _uiState.value.copy(registerPassword = password)
    }

    fun updateRegisterConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(registerConfirmPassword = confirmPassword)
    }

    fun updateRegisterNickname(nickname: String) {
        _uiState.value = _uiState.value.copy(registerNickname = nickname)
    }

    fun updateRegisterPhone(phone: String) {
        _uiState.value = _uiState.value.copy(registerPhone = phone)
    }

    fun updateVerificationCode(code: String) {
        _uiState.value = _uiState.value.copy(verificationCode = code)
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    fun toggleAgreeToTerms() {
        _uiState.value = _uiState.value.copy(
            agreeToTerms = !_uiState.value.agreeToTerms
        )
    }

    fun toggleAgreeToPrivacy() {
        _uiState.value = _uiState.value.copy(
            agreeToPrivacy = !_uiState.value.agreeToPrivacy
        )
    }

    fun sendEmailVerificationCode() {
        val currentState = _uiState.value
        val email = currentState.registerEmail.trim()

        if (email.isBlank() || !email.contains("@")) {
            _uiState.value = currentState.copy(error = "请输入有效的邮箱地址")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            try {
                val result = authRepository.sendVerificationCode(
                    target = email,
                    type = "email",
                    purpose = "register"
                )

                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "验证码已发送到您的邮箱，请查收"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "发送验证码失败，请重试"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "网络连接失败，请检查网络设置"
                )
            }
        }
    }

    fun register() {
        val currentState = _uiState.value

        // 表单验证
        val validationError = validateRegisterForm(currentState)
        if (validationError != null) {
            _uiState.value = currentState.copy(error = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            try {
                val deviceInfo = getDeviceInfo()
                val result = authRepository.register(
                    username = currentState.registerUsername.trim(),
                    email = currentState.registerEmail.trim(),
                    password = currentState.registerPassword,
                    confirmPassword = currentState.registerConfirmPassword,
                    nickname = currentState.registerNickname.trim(),
                    phone = currentState.registerPhone.trim().takeIf { it.isNotBlank() },
                    verificationCode = currentState.verificationCode.trim().takeIf { it.isNotBlank() },
                    deviceInfo = deviceInfo
                )

                result.fold(
                    onSuccess = { registerData ->
                        if (registerData.needVerification) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "注册成功！请查收邮箱验证邮件"
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                successMessage = "注册成功！欢迎加入我们"
                            )
                            
                            // 注册成功后也需要初始化默认数据
                            initializeFirstLoginData()
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "注册失败，请重试"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "网络连接失败，请检查网络设置"
                )
            }
        }
    }

    // ========== 密码重置相关 ==========

    fun updateResetEmail(email: String) {
        _uiState.value = _uiState.value.copy(resetEmail = email)
    }

    fun updateResetCode(code: String) {
        _uiState.value = _uiState.value.copy(resetCode = code)
    }

    fun updateNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(newPassword = password)
    }

    fun updateConfirmNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(confirmNewPassword = password)
    }

    fun sendResetCode() {
        val currentState = _uiState.value
        val email = currentState.resetEmail.trim()

        if (email.isBlank() || !email.contains("@")) {
            _uiState.value = currentState.copy(error = "请输入有效的邮箱地址")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            try {
                val result = authRepository.sendVerificationCode(
                    target = email,
                    type = "email",
                    purpose = "reset"
                )

                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "重置码已发送到您的邮箱，请查收"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "发送重置码失败，请重试"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "网络连接失败，请检查网络设置"
                )
            }
        }
    }

    fun resetPassword() {
        val currentState = _uiState.value

        // 验证表单
        val validationError = validateResetPasswordForm(currentState)
        if (validationError != null) {
            _uiState.value = currentState.copy(error = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            try {
                val result = authRepository.resetPassword(
                    email = currentState.resetEmail.trim(),
                    code = currentState.resetCode.trim(),
                    newPassword = currentState.newPassword,
                    confirmPassword = currentState.confirmNewPassword
                )

                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "密码重置成功！请使用新密码登录"
                        )
                        // 清空表单
                        clearResetPasswordForm()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "密码重置失败，请重试"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "网络连接失败，请检查网络设置"
                )
            }
        }
    }

    // ========== 测试模式 ==========

    fun enableTestMode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = authRepository.enableTestMode()
                result.fold(
                    onSuccess = { loginData ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            isTestMode = true,
                            successMessage = "测试模式已启用，已使用测试用户登录"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "启用测试模式失败：${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "启用测试模式失败：${e.message}"
                )
            }
        }
    }

    fun disableTestMode() {
        viewModelScope.launch {
            try {
                authRepository.disableTestMode()
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = false,
                    isTestMode = false,
                    successMessage = "测试模式已禁用"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "禁用测试模式失败：${e.message}"
                )
            }
        }
    }

    // ========== 登出 ==========

    fun logout() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val result = authRepository.logout()
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            isTestMode = false,
                            successMessage = "已安全退出"
                        )
                        // 清空所有表单数据
                        clearAllForms()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "退出登录失败"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "退出登录失败"
                )
            }
        }
    }

    // ========== UI状态管理 ==========

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    // ========== 私有方法 ==========

    private fun validateRegisterForm(state: AuthUiState): String? {
        return when {
            state.registerUsername.isBlank() -> "请输入用户名"
            state.registerUsername.length < 3 -> "用户名至少需要3个字符"
            state.registerNickname.isBlank() -> "请输入昵称"
            state.registerEmail.isBlank() -> "请输入邮箱"
            !state.registerEmail.contains("@") -> "请输入有效的邮箱地址"
            state.registerPassword.isBlank() -> "请输入密码"
            state.registerPassword.length < 6 -> "密码至少需要6个字符"
            state.registerConfirmPassword != state.registerPassword -> "两次输入的密码不一致"
            !state.agreeToTerms -> "请同意服务条款"
            !state.agreeToPrivacy -> "请同意隐私政策"
            else -> null
        }
    }

    private fun validateResetPasswordForm(state: AuthUiState): String? {
        return when {
            state.resetEmail.isBlank() -> "请输入邮箱"
            !state.resetEmail.contains("@") -> "请输入有效的邮箱地址"
            state.resetCode.isBlank() -> "请输入重置码"
            state.newPassword.isBlank() -> "请输入新密码"
            state.newPassword.length < 6 -> "密码至少需要6个字符"
            state.confirmNewPassword != state.newPassword -> "两次输入的密码不一致"
            else -> null
        }
    }

    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ),
            deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            platform = "Android",
            osVersion = android.os.Build.VERSION.RELEASE,
            appVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
            } catch (e: Exception) {
                "1.0.0"
            },
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.MODEL
        )
    }

    private fun clearAllForms() {
        _uiState.value = _uiState.value.copy(
            // 登录表单
            username = "",
            password = "",
            isPasswordVisible = false,
            rememberLogin = false,
            
            // 注册表单
            registerUsername = "",
            registerEmail = "",
            registerPassword = "",
            registerConfirmPassword = "",
            registerNickname = "",
            registerPhone = "",
            verificationCode = "",
            isConfirmPasswordVisible = false,
            agreeToTerms = false,
            agreeToPrivacy = false,
            
            // 重置密码表单
            resetEmail = "",
            resetCode = "",
            newPassword = "",
            confirmNewPassword = ""
        )
    }

    private fun clearResetPasswordForm() {
        _uiState.value = _uiState.value.copy(
            resetEmail = "",
            resetCode = "",
            newPassword = "",
            confirmNewPassword = ""
        )
    }

    /**
     * 初始化首次登录的默认数据
     * 检查用户是否为首次登录，如果是则自动加载默认配置
     */
    private fun initializeFirstLoginData() {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "开始检查首次登录数据初始化")
                
                // 检查是否已有打卡项目数据，如果没有则说明是首次登录
                val existingItemCount = database.checkInItemDao().getActiveItemCount()
                Log.d("AuthViewModel", "现有打卡项目数量: $existingItemCount")
                
                if (existingItemCount == 0) {
                    Log.d("AuthViewModel", "检测到首次登录，开始初始化默认数据")
                    
                    // 更新UI状态，告知用户正在初始化
                    _uiState.value = _uiState.value.copy(
                        successMessage = "欢迎首次使用！正在为您准备默认配置..."
                    )
                    
                    // 清理可能存在的测试好友数据
                    DefaultDataInitializer.cleanupTestFriendData(database)
                    
                    // 初始化默认数据（包括打卡项目、AI角色、等级定义等）
                    DefaultDataInitializer.initializeBasicData(context, database)
                    
                    Log.d("AuthViewModel", "首次登录默认数据初始化完成")
                    
                    // 更新成功消息
                    _uiState.value = _uiState.value.copy(
                        successMessage = "登录成功！默认配置已准备就绪，开始您的习惯追踪之旅吧！"
                    )
                } else {
                    Log.d("AuthViewModel", "用户已有数据，跳过默认数据初始化")
                    
                    // 确保AI角色等基础数据存在（即使有打卡项目也可能缺少其他数据）
                    DefaultDataInitializer.initializeBasicData(context, database)
                }
                
            } catch (e: Exception) {
                Log.e("AuthViewModel", "首次登录数据初始化失败", e)
                // 即使初始化失败也不影响登录流程，只是记录错误
                _uiState.value = _uiState.value.copy(
                    successMessage = "登录成功！欢迎使用习惯追踪器！"
                )
            }
        }
    }
}

/**
 * 认证UI状态
 */
data class AuthUiState(
    // 通用状态
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val isTestMode: Boolean = false,
    val currentUser: com.example.cur_app.data.repository.AuthUserInfo? = null,

    // 登录表单
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberLogin: Boolean = false,

    // 注册表单
    val registerUsername: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerConfirmPassword: String = "",
    val registerNickname: String = "",
    val registerPhone: String = "",
    val verificationCode: String = "",
    val isConfirmPasswordVisible: Boolean = false,
    val agreeToTerms: Boolean = false,
    val agreeToPrivacy: Boolean = false,

    // 重置密码表单
    val resetEmail: String = "",
    val resetCode: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = ""
)