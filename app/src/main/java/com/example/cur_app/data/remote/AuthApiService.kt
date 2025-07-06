package com.example.cur_app.data.remote

import com.example.cur_app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 用户认证相关的远程API接口
 * 提供登录、注册、用户信息管理等功能
 */
interface AuthApiService {
    
    // ========== 用户认证 ==========
    
    /**
     * 用户登录
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    /**
     * 用户注册
     */
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    /**
     * 刷新访问令牌
     */
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>
    
    /**
     * 用户登出
     */
    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<BaseResponse>
    
    /**
     * 验证邮箱
     */
    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<BaseResponse>
    
    /**
     * 发送验证码
     */
    @POST("auth/send-verification-code")
    suspend fun sendVerificationCode(@Body request: SendCodeRequest): Response<BaseResponse>
    
    /**
     * 重置密码
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<BaseResponse>
    
    // ========== 用户信息管理 ==========
    
    /**
     * 获取用户信息
     */
    @GET("user/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserProfileResponse>
    
    /**
     * 更新用户信息
     */
    @PUT("user/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UserProfileResponse>
    
    /**
     * 上传用户头像
     */
    @Multipart
    @POST("user/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,
        @Part("avatar") avatar: okhttp3.MultipartBody.Part
    ): Response<UploadAvatarResponse>
    
    /**
     * 更新密码
     */
    @PUT("user/password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<BaseResponse>
    
    // ========== 好友管理 ==========
    
    // TODO: 迁移到FriendApiService
    // /**
    //  * 搜索用户
    //  */
    // @GET("user/search")
    // suspend fun searchUsers(
    //     @Header("Authorization") token: String,
    //     @Query("keyword") keyword: String,
    //     @Query("page") page: Int = 1,
    //     @Query("size") size: Int = 20
    // ): Response<SearchUsersResponse>
    
    // /**
    //  * 根据UID搜索用户
    //  */
    // @GET("api/users/search/{uid}")
    // suspend fun searchUserByUid(
    //     @Header("Authorization") token: String,
    //     @Path("uid") uid: String
    // ): Response<SearchUserByUidResponse>
    
    /**
     * 发送好友请求
     */
    @POST("api/users/friends/request")
    suspend fun sendFriendRequest(
        @Header("Authorization") token: String,
        @Body request: FriendRequestBody
    ): Response<BaseResponse>
    
    /**
     * 处理好友请求（接受/拒绝）
     */
    @POST("api/users/friends/requests/{requestId}")
    suspend fun handleFriendRequest(
        @Header("Authorization") token: String,
        @Path("requestId") requestId: String,
        @Body request: HandleFriendRequestBody
    ): Response<BaseResponse>
    
    /**
     * 获取好友列表
     */
    @GET("api/users/friends")
    suspend fun getFriendList(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50
    ): Response<FriendListResponse>
    
    /**
     * 获取好友请求列表
     */
    @GET("api/users/friends/requests")
    suspend fun getFriendRequests(
        @Header("Authorization") token: String,
        @Query("type") type: String = "received" // received, sent
    ): Response<FriendRequestsResponse>
    
    /**
     * 删除好友
     */
    @DELETE("api/users/friends/{friendId}")
    suspend fun deleteFriend(
        @Header("Authorization") token: String,
        @Path("friendId") friendId: String
    ): Response<BaseResponse>
    
    // ========== 聊天相关 ==========
    
    /**
     * 获取聊天记录
     */
    @GET("chat/conversations/{conversationId}/messages")
    suspend fun getChatMessages(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50
    ): Response<ChatMessagesResponse>
    
    /**
     * 发送消息
     */
    @POST("chat/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>
    
    /**
     * 获取对话列表
     */
    @GET("chat/conversations")
    suspend fun getConversations(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ConversationsResponse>
    
    /**
     * 创建对话
     */
    @POST("chat/conversations")
    suspend fun createConversation(
        @Header("Authorization") token: String,
        @Body request: CreateConversationRequest
    ): Response<CreateConversationResponse>
    
    /**
     * 标记消息为已读
     */
    @POST("chat/conversations/{conversationId}/read")
    suspend fun markAsRead(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: String
    ): Response<BaseResponse>
    
    // ========== 服务器状态检测 ==========
    
    /**
     * 服务器健康检查
     */
    @GET("health")
    suspend fun healthCheck(): Response<HealthCheckResponse>
    
    /**
     * 服务器连接测试
     */
    @GET("api/ping")
    suspend fun ping(): Response<PingResponse>
    
    // ========== AI角色管理 ==========
    
    /**
     * 获取AI角色列表
     */
    @GET("ai/characters")
    suspend fun getAICharacters(
        @Header("Authorization") token: String
    ): Response<AICharactersResponse>
    
    /**
     * 与AI角色聊天
     */
    @POST("ai/chat")
    suspend fun chatWithAI(
        @Header("Authorization") token: String,
        @Body request: AIChatRequest
    ): Response<AIChatResponse>
    
    /**
     * 更新AI角色配置
     */
    @PUT("ai/characters/{characterId}")
    suspend fun updateAICharacter(
        @Header("Authorization") token: String,
        @Path("characterId") characterId: String,
        @Body request: UpdateAICharacterRequest
    ): Response<AICharacterResponse>
}