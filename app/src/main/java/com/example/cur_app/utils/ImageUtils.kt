package com.example.cur_app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 图片工具类
 * 负责用户头像的管理、存储和显示
 */
object ImageUtils {
    
    // 用户头像存储目录名
    private const val USER_AVATAR_DIR = "user_avatars"
    
    // 默认emoji头像列表
    val DEFAULT_EMOJI_AVATARS = listOf(
        "😊", "😎", "🥰", "🤔", "😴", "🤓", "😋", "🥳", 
        "🤗", "😌", "🙂", "😇", "🤠", "🥺", "😍", "🤩"
    )
    
    /**
     * 获取用户头像存储目录
     */
    private fun getUserAvatarDir(context: Context): File {
        val dir = File(context.filesDir, USER_AVATAR_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * 保存用户头像到本地（挂起函数，在后台线程执行）
     */
    suspend fun saveUserAvatar(context: Context, bitmap: Bitmap, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val avatarDir = getUserAvatarDir(context)
                val file = File(avatarDir, "$fileName.jpg")
                
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                
                file.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 从URI保存用户头像（挂起函数）
     */
    suspend fun saveUserAvatarFromUri(context: Context, uri: Uri, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap != null) {
                    // 压缩和裁剪图片
                    val compressedBitmap = compressAndCropAvatar(bitmap)
                    saveUserAvatar(context, compressedBitmap, fileName)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 压缩和裁剪头像为正方形
     */
    private fun compressAndCropAvatar(bitmap: Bitmap, size: Int = 300): Bitmap {
        // 计算最小边长用于裁剪正方形
        val minSize = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - minSize) / 2
        val y = (bitmap.height - minSize) / 2
        
        // 裁剪为正方形
        val squareBitmap = Bitmap.createBitmap(bitmap, x, y, minSize, minSize)
        
        // 缩放到指定大小
        return Bitmap.createScaledBitmap(squareBitmap, size, size, true)
    }
    
    /**
     * 加载图片文件为Bitmap（挂起函数）
     */
    suspend fun loadImageFile(filePath: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(filePath)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 删除用户头像文件
     */
    fun deleteUserAvatar(context: Context, fileName: String): Boolean {
        return try {
            val avatarDir = getUserAvatarDir(context)
            val file = File(avatarDir, "$fileName.jpg")
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 检查头像文件是否存在
     */
    fun avatarFileExists(context: Context, fileName: String): Boolean {
        val avatarDir = getUserAvatarDir(context)
        val file = File(avatarDir, "$fileName.jpg")
        return file.exists()
    }
    
    /**
     * 生成唯一的头像文件名
     */
    fun generateAvatarFileName(): String {
        return "avatar_${System.currentTimeMillis()}"
    }
    
    /**
     * 清理所有用户头像
     */
    fun clearAllUserAvatars(context: Context): Boolean {
        return try {
            val avatarDir = getUserAvatarDir(context)
            avatarDir.listFiles()?.forEach { file ->
                file.delete()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/**
 * 用户头像显示组件
 * 安全地加载和显示用户头像
 */
@Composable
fun UserAvatarDisplay(
    avatarType: String,
    avatarValue: String,
    size: Dp = 80.dp,
    fontSize: TextUnit = 32.sp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var imageBitmap by remember(avatarValue) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(avatarValue) { mutableStateOf(false) }
    
    // 当头像值改变时，重新加载图片
    LaunchedEffect(avatarValue, avatarType) {
        if (avatarType == "image") {
            isLoading = true
            imageBitmap = ImageUtils.loadImageFile(avatarValue)
            isLoading = false
        }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            avatarType == "emoji" -> {
                Text(
                    text = avatarValue,
                    fontSize = fontSize
                )
            }
            avatarType == "image" && isLoading -> {
                Text(
                    text = "⏳",
                    fontSize = fontSize
                )
            }
            avatarType == "image" && imageBitmap != null -> {
                Image(
                    bitmap = imageBitmap!!.asImageBitmap(),
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                // 默认显示emoji或图片加载失败时的占位符
                Text(
                    text = "😊",
                    fontSize = fontSize
                )
            }
        }
    }
} 