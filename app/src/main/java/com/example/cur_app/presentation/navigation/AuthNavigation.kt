package com.example.cur_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cur_app.presentation.screens.auth.LoginScreen
import com.example.cur_app.presentation.screens.auth.RegisterScreen
import com.example.cur_app.presentation.screens.auth.ForgotPasswordScreen
import com.example.cur_app.presentation.viewmodel.AuthViewModel

/**
 * 认证相关的导航组件
 * 处理登录、注册、忘记密码等页面之间的导航
 */
@Composable
fun AuthNavigation(
    navController: NavHostController = rememberNavController(),
    onNavigateToMain: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    
    // 监听登录状态，如果已登录则导航到主页面
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onNavigateToMain()
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = AuthScreens.Login.route
    ) {
        composable(AuthScreens.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AuthScreens.Register.route)
                },
                onNavigateToHome = onNavigateToMain,
                onNavigateToForgotPassword = {
                    navController.navigate(AuthScreens.ForgotPassword.route)
                },
                viewModel = authViewModel
            )
        }
        
        composable(AuthScreens.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(AuthScreens.Login.route) {
                        popUpTo(AuthScreens.Login.route) { inclusive = true }
                    }
                },
                onNavigateToHome = onNavigateToMain,
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }
        
        composable(AuthScreens.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(AuthScreens.Login.route) {
                        popUpTo(AuthScreens.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }
    }
}

/**
 * 认证相关的屏幕路由定义
 */
sealed class AuthScreens(val route: String) {
    object Login : AuthScreens("auth_login")
    object Register : AuthScreens("auth_register")
    object ForgotPassword : AuthScreens("auth_forgot_password")
}