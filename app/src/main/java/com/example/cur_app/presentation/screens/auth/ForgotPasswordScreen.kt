package com.example.cur_app.presentation.screens.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cur_app.presentation.viewmodel.AuthViewModel

/**
 * 忘记密码界面
 * 支持邮箱验证和密码重置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    
    var currentStep by remember { mutableIntStateOf(1) } // 1: 输入邮箱, 2: 输入验证码和新密码
    
    // 监听成功消息，如果重置成功则返回登录页
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage == "密码重置成功！请使用新密码登录") {
            kotlinx.coroutines.delay(2000)
            onNavigateToLogin()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2),
                        Color(0xFF6B73FF)
                    )
                )
            )
    ) {
        // 装饰性圆圈
        ForgotPasswordDecorativeCircles()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部导航
            TopNavigationBar(onNavigateBack = onNavigateBack)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 标题区域
            ForgotPasswordHeaderSection(currentStep)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 步骤指示器
            StepIndicator(currentStep = currentStep, totalSteps = 2)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 表单内容
            when (currentStep) {
                1 -> EmailInputStep(
                    email = uiState.resetEmail,
                    isLoading = uiState.isLoading,
                    onEmailChange = viewModel::updateResetEmail,
                    onSendCode = {
                        viewModel.sendResetCode()
                        // 发送成功后进入下一步
                        if (uiState.successMessage?.contains("重置码已发送") == true) {
                            currentStep = 2
                        }
                    },
                    focusManager = focusManager
                )
                2 -> ResetPasswordStep(
                    email = uiState.resetEmail,
                    code = uiState.resetCode,
                    newPassword = uiState.newPassword,
                    confirmPassword = uiState.confirmNewPassword,
                    isLoading = uiState.isLoading,
                    onCodeChange = viewModel::updateResetCode,
                    onNewPasswordChange = viewModel::updateNewPassword,
                    onConfirmPasswordChange = viewModel::updateConfirmNewPassword,
                    onResetPassword = viewModel::resetPassword,
                    onResendCode = viewModel::sendResetCode,
                    onBackToEmail = { currentStep = 1 },
                    focusManager = focusManager
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 返回登录链接
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "返回登录",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // 错误和成功提示
        if (uiState.error != null) {
            ForgotPasswordErrorSnackbar(
                error = uiState.error!!,
                onDismiss = viewModel::clearError,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        if (uiState.successMessage != null) {
            ForgotPasswordSuccessSnackbar(
                message = uiState.successMessage!!,
                onDismiss = viewModel::clearSuccessMessage,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
    
    // 监听发送验证码成功，自动进入下一步
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("重置码已发送") == true && currentStep == 1) {
            kotlinx.coroutines.delay(1500)
            currentStep = 2
        }
    }
}

@Composable
private fun TopNavigationBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ForgotPasswordHeaderSection(currentStep: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (currentStep == 1) Icons.Default.Email else Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        
        // 标题
        Text(
            text = when (currentStep) {
                1 -> "重置密码"
                2 -> "设置新密码"
                else -> "重置密码"
            },
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        // 副标题
        Text(
            text = when (currentStep) {
                1 -> "请输入您的邮箱地址\n我们将发送重置码到您的邮箱"
                2 -> "请输入收到的重置码\n并设置您的新密码"
                else -> "找回您的账号密码"
            },
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1
            val isActive = stepNumber <= currentStep
            val isCurrent = stepNumber == currentStep
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) Color.White else Color.White.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (stepNumber < currentStep) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = stepNumber.toString(),
                        color = if (isActive) Color(0xFF667eea) else Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(
                            if (stepNumber < currentStep) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
private fun EmailInputStep(
    email: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onSendCode: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 邮箱输入框
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("邮箱地址", color = Color.White.copy(alpha = 0.8f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (email.isNotBlank() && email.contains("@")) {
                            onSendCode()
                        }
                    }
                ),
                singleLine = true
            )
            
            // 发送重置码按钮
            Button(
                onClick = onSendCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF667eea)
                ),
                enabled = !isLoading && email.isNotBlank() && email.contains("@")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF667eea),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "发送重置码",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ResetPasswordStep(
    email: String,
    code: String,
    newPassword: String,
    confirmPassword: String,
    isLoading: Boolean,
    onCodeChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetPassword: () -> Unit,
    onResendCode: () -> Unit,
    onBackToEmail: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 邮箱显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = email,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onBackToEmail) {
                    Text(
                        text = "更改",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
            
            // 重置码输入框
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    label = { Text("重置码", color = Color.White.copy(alpha = 0.8f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true
                )
                
                OutlinedButton(
                    onClick = onResendCode,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "重发",
                        fontSize = 12.sp
                    )
                }
            }
            
            // 新密码输入框
            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text("新密码", color = Color.White.copy(alpha = 0.8f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Text(
                            text = if (isPasswordVisible) "🙈" else "👁️",
                            fontSize = 18.sp
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )
            
            // 确认新密码输入框
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("确认新密码", color = Color.White.copy(alpha = 0.8f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Text(
                            text = if (isConfirmPasswordVisible) "🙈" else "👁️",
                            fontSize = 18.sp
                        )
                    }
                },
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (isResetFormValid(code, newPassword, confirmPassword)) {
                            onResetPassword()
                        }
                    }
                ),
                singleLine = true
            )
            
            // 重置密码按钮
            val isFormValid = isResetFormValid(code, newPassword, confirmPassword)
            Button(
                onClick = onResetPassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF667eea)
                ),
                enabled = !isLoading && isFormValid
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF667eea),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "重置密码",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun isResetFormValid(
    code: String,
    newPassword: String,
    confirmPassword: String
): Boolean {
    return code.isNotBlank() &&
            newPassword.isNotBlank() && newPassword.length >= 6 &&
            confirmPassword == newPassword
}

@Composable
private fun ForgotPasswordDecorativeCircles() {
    // 装饰性背景圆圈
    Box(modifier = Modifier.fillMaxSize()) {
        // 大圆圈 - 左上
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset((-50).dp, (-50).dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.1f)
                )
                .align(Alignment.TopStart)
        )
        
        // 中圆圈 - 右上
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(30.dp, (-20).dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.08f)
                )
                .align(Alignment.TopEnd)
        )
        
        // 小圆圈 - 左下
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset((-20).dp, 50.dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.06f)
                )
                .align(Alignment.BottomStart)
        )
        
        // 大圆圈 - 右下
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(40.dp, 60.dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.05f)
                )
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun ForgotPasswordErrorSnackbar(
    error: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Red.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = error,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ForgotPasswordSuccessSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Green.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}