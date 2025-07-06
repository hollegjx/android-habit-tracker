package com.example.cur_app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * 图片选择工具类
 * 处理图片选择、权限请求和结果处理
 */
object ImagePickerUtils {
    
    /**
     * 检查是否有读取外部存储权限
     */
    fun hasReadStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13及以上版本使用READ_MEDIA_IMAGES权限
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12及以下版本使用READ_EXTERNAL_STORAGE权限
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 获取所需的权限列表
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}

/**
 * 图片选择器Composable组件
 * 提供完整的图片选择和权限处理功能
 */
@Composable
fun rememberImagePicker(
    onImageSelected: (Uri) -> Unit,
    onPermissionDenied: () -> Unit = {}
): ImagePickerState {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }
    
    // 权限请求器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            imagePickerLauncher.launch("image/*")
        } else {
            onPermissionDenied()
        }
    }
    
    return remember {
        ImagePickerState(
            onPickImage = {
                if (ImagePickerUtils.hasReadStoragePermission(context)) {
                    imagePickerLauncher.launch("image/*")
                } else {
                    permissionLauncher.launch(ImagePickerUtils.getRequiredPermissions())
                }
            }
        )
    }
}

/**
 * 图片选择器状态类
 */
class ImagePickerState(
    val onPickImage: () -> Unit
)

/**
 * 权限对话框状态管理
 */
@Composable
fun PermissionHandler(
    permissions: Array<String>,
    onPermissionResult: (Map<String, Boolean>) -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        onPermissionResult(result)
    }
    
    LaunchedEffect(permissions) {
        permissionLauncher.launch(permissions)
    }
} 