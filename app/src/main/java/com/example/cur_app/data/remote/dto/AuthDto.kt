package com.example.cur_app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== 基础响应类型 ==========

/**
 * 基础API响应
 */
data class BaseResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 带数据的API响应
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

// ========== 登录相关 ==========

/**
 * 登录请求
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String, // 可以是邮箱、手机号或用户名
    @SerializedName("password")
    val password: String,
    @SerializedName("deviceInfo")
    val deviceInfo: DeviceInfo? = null
)

/**
 * 登录响应
 */
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: LoginData? = null
)

/**
 * 登录数据
 */
data class LoginData(
    @SerializedName("user")
    val user: RemoteUserProfile,
    @SerializedName("token")
    val token: TokenInfo,
    @SerializedName("permissions")
    val permissions: List<String> = emptyList(),
    @SerializedName("settings")
    val settings: UserSettings? = null
)

/**
 * Token信息
 */
data class TokenInfo(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("tokenType")
    val tokenType: String = "Bearer",
    @SerializedName("expiresIn")
    val expiresIn: Long, // 秒
    @SerializedName("scope")
    val scope: String? = null
)

// ========== 注册相关 ==========

/**
 * 注册请求
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("confirmPassword")
    val confirmPassword: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("verificationCode")
    val verificationCode: String? = null,
    @SerializedName("inviteCode")
    val inviteCode: String? = null,
    @SerializedName("agreeTOS")
    val agreeTOS: Boolean = true,
    @SerializedName("agreePrivacy")
    val agreePrivacy: Boolean = true,
    @SerializedName("deviceInfo")
    val deviceInfo: DeviceInfo? = null
)

/**
 * 注册响应
 */
data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: RegisterData? = null
)

/**
 * 注册数据
 */
data class RegisterData(
    @SerializedName("user")
    val user: RemoteUserProfile,
    @SerializedName("token")
    val token: TokenInfo? = null, // 可能需要邮箱验证后才提供token
    @SerializedName("needVerification")
    val needVerification: Boolean = false,
    @SerializedName("verificationMethod")
    val verificationMethod: String? = null // email, sms
)

// ========== 用户信息相关 ==========

/**
 * 远程用户资料
 */
data class RemoteUserProfile(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("realName")
    val realName: String? = null,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("signature")
    val signature: String? = null,
    @SerializedName("gender")
    val gender: String? = null, // male, female, other
    @SerializedName("birthday")
    val birthday: String? = null, // yyyy-MM-dd
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("website")
    val website: String? = null,
    @SerializedName("isVerified")
    val isVerified: Boolean = false,
    @SerializedName("verificationLevel")
    val verificationLevel: String? = null,
    @SerializedName("level")
    val level: Int = 1,
    @SerializedName("experience")
    val experience: Long = 0,
    @SerializedName("status")
    val status: String = "active", // active, inactive, banned
    @SerializedName("isOnline")
    val isOnline: Boolean = false,
    @SerializedName("lastSeenTime")
    val lastSeenTime: Long? = null,
    @SerializedName("joinedAt")
    val joinedAt: Long,
    @SerializedName("updatedAt")
    val updatedAt: Long
)

/**
 * 用户设置
 */
data class UserSettings(
    @SerializedName("language")
    val language: String = "zh-CN",
    @SerializedName("theme")
    val theme: String = "system", // light, dark, system
    @SerializedName("notificationEnabled")
    val notificationEnabled: Boolean = true,
    @SerializedName("soundEnabled")
    val soundEnabled: Boolean = true,
    @SerializedName("vibrationEnabled")
    val vibrationEnabled: Boolean = true,
    @SerializedName("privacySettings")
    val privacySettings: PrivacySettings? = null
)

/**
 * 隐私设置
 */
data class PrivacySettings(
    @SerializedName("showOnlineStatus")
    val showOnlineStatus: Boolean = true,
    @SerializedName("allowFriendRequests")
    val allowFriendRequests: Boolean = true,
    @SerializedName("allowSearch")
    val allowSearch: Boolean = true,
    @SerializedName("showPhoneNumber")
    val showPhoneNumber: Boolean = false,
    @SerializedName("showEmail")
    val showEmail: Boolean = false
)

/**
 * 设备信息
 */
data class DeviceInfo(
    @SerializedName("deviceId")
    val deviceId: String,
    @SerializedName("deviceName")
    val deviceName: String,
    @SerializedName("platform")
    val platform: String = "Android",
    @SerializedName("osVersion")
    val osVersion: String,
    @SerializedName("appVersion")
    val appVersion: String,
    @SerializedName("manufacturer")
    val manufacturer: String? = null,
    @SerializedName("model")
    val model: String? = null
)

// ========== Token相关 ==========

/**
 * 刷新Token请求
 */
data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("deviceId")
    val deviceId: String? = null
)

/**
 * 刷新Token响应
 */
data class RefreshTokenResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: TokenInfo? = null
)

// ========== 验证相关 ==========

/**
 * 邮箱验证请求
 */
data class VerifyEmailRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("type")
    val type: String = "register" // register, reset, change
)

/**
 * 发送验证码请求
 */
data class SendCodeRequest(
    @SerializedName("target")
    val target: String, // 邮箱或手机号
    @SerializedName("type")
    val type: String, // email, sms
    @SerializedName("purpose")
    val purpose: String // register, login, reset, verify
)

/**
 * 重置密码请求
 */
data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("newPassword")
    val newPassword: String,
    @SerializedName("confirmPassword")
    val confirmPassword: String
)

// ========== 用户资料更新相关 ==========

/**
 * 用户资料响应
 */
data class UserProfileResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: RemoteUserProfile? = null
)

/**
 * 更新资料请求
 */
data class UpdateProfileRequest(
    @SerializedName("nickname")
    val nickname: String? = null,
    @SerializedName("realName")
    val realName: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("signature")
    val signature: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("birthday")
    val birthday: String? = null,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("website")
    val website: String? = null
)

/**
 * 上传头像响应
 */
data class UploadAvatarResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: AvatarData? = null
)

/**
 * 头像数据
 */
data class AvatarData(
    @SerializedName("url")
    val url: String,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,
    @SerializedName("fileId")
    val fileId: String? = null
)

/**
 * 修改密码请求
 */
data class ChangePasswordRequest(
    @SerializedName("oldPassword")
    val oldPassword: String,
    @SerializedName("newPassword")
    val newPassword: String,
    @SerializedName("confirmPassword")
    val confirmPassword: String
)