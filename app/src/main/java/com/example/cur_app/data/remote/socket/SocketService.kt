package com.example.cur_app.data.remote.socket

import android.util.Log
import com.example.cur_app.data.remote.dto.RemoteChatMessage
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Socket.IO 客户端服务
 * 负责与后端建立实时连接，处理聊天消息的发送和接收
 */
@Singleton
class SocketService @Inject constructor(
    private val gson: Gson
) {
    companion object {
        private const val TAG = "SocketService"
        private const val SOCKET_URL = com.example.cur_app.network.ServerConfig.SOCKET_URL
    }

    private var socket: Socket? = null
    private var isConnected = false
    private var accessToken: String? = null

    // 消息接收通道
    private val _messageReceived = Channel<RemoteChatMessage>(Channel.UNLIMITED)
    val messageReceived: Flow<RemoteChatMessage> = _messageReceived.receiveAsFlow()

    // 连接状态通道
    private val _connectionStatus = Channel<Boolean>(Channel.UNLIMITED)
    val connectionStatus: Flow<Boolean> = _connectionStatus.receiveAsFlow()

    // 错误通道
    private val _errors = Channel<String>(Channel.UNLIMITED)
    val errors: Flow<String> = _errors.receiveAsFlow()

    /**
     * 连接到Socket.IO服务器
     */
    fun connect(token: String) {
        if (isConnected) {
            Log.d(TAG, "Socket already connected")
            return
        }

        accessToken = token
        
        try {
            val opts = IO.Options().apply {
                auth = mapOf("token" to token)
                transports = arrayOf("websocket")
                timeout = 5000  // 减少超时时间
                reconnection = false  // 禁用自动重连以避免持续错误
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                reconnectionAttempts = 0  // 不重试连接
            }

            socket = IO.socket(SOCKET_URL, opts)
            
            setupSocketListeners()
            socket?.connect()
            
            Log.d(TAG, "Socket connection initiated (non-blocking)")
        } catch (e: URISyntaxException) {
            Log.w(TAG, "Socket connection failed: ${e.message} - continuing without real-time features")
            // 不发送错误到UI，因为这不是关键功能
        } catch (e: Exception) {
            Log.w(TAG, "Socket connection error: ${e.message} - continuing without real-time features")
            // 不发送错误到UI，因为这不是关键功能
        }
    }

    /**
     * 断开Socket连接
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        isConnected = false
        accessToken = null
        _connectionStatus.trySend(false)
        Log.d(TAG, "Socket disconnected")
    }

    /**
     * 加入对话房间
     */
    fun joinConversation(conversationId: String) {
        if (isConnected) {
            socket?.emit("join_conversation", conversationId)
            Log.d(TAG, "Joined conversation: $conversationId")
        } else {
            Log.w(TAG, "Cannot join conversation: socket not connected")
        }
    }

    /**
     * 离开对话房间
     */
    fun leaveConversation(conversationId: String) {
        if (isConnected) {
            socket?.emit("leave_conversation", conversationId)
            Log.d(TAG, "Left conversation: $conversationId")
        }
    }

    /**
     * 发送聊天消息
     */
    fun sendMessage(
        conversationId: String,
        content: String,
        messageType: String = "text",
        replyToId: String? = null
    ) {
        if (!isConnected) {
            Log.d(TAG, "Socket not connected - message will be stored locally only")
            // 不发送错误，消息会存储在本地数据库中
            return
        }

        val messageData = mapOf(
            "conversationId" to conversationId,
            "content" to content,
            "messageType" to messageType,
            "replyToId" to replyToId,
            "timestamp" to System.currentTimeMillis()
        )

        socket?.emit("send_message", gson.toJson(messageData))
        Log.d(TAG, "Message sent to conversation: $conversationId")
    }

    /**
     * 标记消息为已读
     */
    fun markAsRead(conversationId: String, messageIds: List<String>) {
        if (isConnected) {
            val data = mapOf(
                "conversationId" to conversationId,
                "messageIds" to messageIds
            )
            socket?.emit("mark_as_read", gson.toJson(data))
            Log.d(TAG, "Marked messages as read in conversation: $conversationId")
        }
    }

    /**
     * 发送正在输入状态
     */
    fun sendTyping(conversationId: String, isTyping: Boolean) {
        if (isConnected) {
            val data = mapOf(
                "conversationId" to conversationId,
                "isTyping" to isTyping
            )
            socket?.emit("typing", gson.toJson(data))
        }
    }

    /**
     * 设置Socket事件监听器
     */
    private fun setupSocketListeners() {
        socket?.apply {
            // 连接成功
            on(Socket.EVENT_CONNECT) {
                isConnected = true
                _connectionStatus.trySend(true)
                Log.d(TAG, "Socket connected successfully")
            }

            // 连接断开
            on(Socket.EVENT_DISCONNECT) {
                isConnected = false
                _connectionStatus.trySend(false)
                Log.d(TAG, "Socket disconnected")
            }

            // 连接错误
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                isConnected = false
                _connectionStatus.trySend(false)
                val error = args[0]?.toString() ?: "Unknown connection error"
                Log.w(TAG, "Socket connection error: $error - real-time features unavailable")
                // 不发送错误到UI，因为Socket连接失败不应该影响用户体验
            }

            // 重连尝试
            on("reconnecting") { args ->
                Log.d(TAG, "Socket reconnecting...")
            }
            
            // 重连成功
            on("reconnect") { args ->
                Log.d(TAG, "Socket reconnected")
                // 重连后重新加入之前的对话房间
                accessToken?.let { token ->
                    socket?.emit("authenticate", token)
                }
            }

            // 接收新消息
            on("new_message") { args ->
                try {
                    val messageJson = args[0].toString()
                    val message = gson.fromJson(messageJson, RemoteChatMessage::class.java)
                    _messageReceived.trySend(message)
                    Log.d(TAG, "Received new message: ${message.messageId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing received message: ${e.message}")
                }
            }

            // 消息状态更新（已读、已送达等）
            on("message_status_update") { args ->
                try {
                    val statusJson = args[0].toString()
                    Log.d(TAG, "Message status updated: $statusJson")
                    // TODO: 处理消息状态更新
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message status: ${e.message}")
                }
            }

            // 用户正在输入
            on("user_typing") { args ->
                try {
                    val typingJson = args[0].toString()
                    Log.d(TAG, "User typing: $typingJson")
                    // TODO: 处理正在输入状态
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing typing status: ${e.message}")
                }
            }

            // 服务器错误
            on("error") { args ->
                val error = args[0]?.toString() ?: "Unknown server error"
                Log.e(TAG, "Server error: $error")
                _errors.trySend("服务器错误: $error")
            }

            // 认证失败
            on("auth_error") { args ->
                val error = args[0]?.toString() ?: "Authentication failed"
                Log.e(TAG, "Auth error: $error")
                _errors.trySend("认证失败: $error")
                disconnect()
            }
        }
    }

    /**
     * 检查连接状态
     */
    fun isConnected(): Boolean = isConnected

    /**
     * 获取Socket实例（用于调试）
     */
    fun getSocketInstance(): Socket? = socket
}