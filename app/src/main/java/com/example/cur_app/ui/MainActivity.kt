package com.example.cur_app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cur_app.presentation.components.BottomNavigationBar
import com.example.cur_app.presentation.navigation.AppNavigation
import com.example.cur_app.presentation.navigation.HabitTrackerNavigation
import com.example.cur_app.ui.theme.Cur_appTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

/**
 * AI习惯追踪应用主Activity
 * 使用Jetpack Compose构建UI，集成导航和底部导航栏
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 设置状态栏和导航栏透明
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            Cur_appTheme {
                // 基础沉浸式设置 - 状态栏由各个页面的DynamicThemeBackground控制
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    // 设置导航栏透明，状态栏由各个页面单独控制
                    systemUiController.setNavigationBarColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )
                }
                
                HabitTrackerApp()
            }
        }
    }
}

@Composable
fun HabitTrackerApp() {
    // 使用认证感知的应用导航
    AppNavigation()
} 