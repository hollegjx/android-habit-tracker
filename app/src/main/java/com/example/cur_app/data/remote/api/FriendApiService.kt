package com.example.cur_app.data.remote.api

import com.example.cur_app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface FriendApiService {
    
    @GET("api/friends/search/{uid}")
    suspend fun searchUserByUid(@Path("uid") uid: String): Response<FriendSearchResponse>
    
    @POST("api/friends/request")
    suspend fun sendFriendRequest(@Body request: FriendRequestBody): Response<BaseResponse>
    
    @GET("api/friends/requests")
    suspend fun getFriendRequests(@Query("type") type: String = "received"): Response<FriendRequestsResponse>
    
    @POST("api/friends/requests/{requestId}")
    suspend fun handleFriendRequest(@Path("requestId") requestId: String, @Body request: HandleFriendRequestBody): Response<BaseResponse>
    
    @GET("api/friends")
    suspend fun getFriendList(): Response<FriendListResponse>
    
    @DELETE("api/friends/{friendId}")
    suspend fun removeFriend(@Path("friendId") friendId: String): Response<BaseResponse>
    
    @PUT("api/friends/{friendId}/settings")
    suspend fun updateFriendSettings(@Path("friendId") friendId: String, @Body request: FriendSettingsRequest): Response<BaseResponse>
    
    @GET("api/friends/notifications")
    suspend fun getFriendNotifications(@Query("limit") limit: Int = 20, @Query("offset") offset: Int = 0): Response<FriendNotificationsResponse>
    
    @POST("api/friends/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: String): Response<BaseResponse>
    
    @POST("api/friends/notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<BaseResponse>
    
    @GET("api/friends/health")
    suspend fun healthCheck(): Response<BaseResponse>
}