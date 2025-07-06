package com.example.cur_app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cur_app.R
import com.example.cur_app.presentation.viewmodel.AddEditHabitViewModel
import com.example.cur_app.presentation.viewmodel.AddEditHabitUiState

/**
 * 添加/编辑习惯界面
 * 支持创建新习惯或编辑现有习惯
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    onNavigateBack: () -> Unit,
    habitId: Long? = null,
    viewModel: AddEditHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditMode = habitId != null
    
    // 初始化编辑模式
    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.loadHabit(habitId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) {
                            stringResource(R.string.edit_habit)
                        } else {
                            stringResource(R.string.add_habit)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // 基本信息
            BasicInfoSection(
                uiState = uiState,
                viewModel = viewModel
            )
            
            // 分类和图标
            CategoryIconSection(
                uiState = uiState,
                viewModel = viewModel
            )
            
            // 频率设置
            FrequencySection(
                uiState = uiState,
                viewModel = viewModel
            )
            
            // 提醒设置
            ReminderSection(
                uiState = uiState,
                viewModel = viewModel
            )
            
            // 保存按钮
            Button(
                onClick = {
                    if (isEditMode) {
                        viewModel.updateHabit()
                    } else {
                        viewModel.createHabit()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = !uiState.isLoading && uiState.isFormValid
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (isEditMode) {
                            stringResource(R.string.update_habit)
                        } else {
                            stringResource(R.string.create_habit)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // 处理保存成功
    LaunchedEffect(uiState.isHabitSaved) {
        if (uiState.isHabitSaved) {
            onNavigateBack()
        }
    }
    
    // 处理错误提示
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // TODO: 显示Snackbar错误提示
        }
    }
}

@Composable
private fun BasicInfoSection(
    uiState: AddEditHabitUiState,
    viewModel: AddEditHabitViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.basic_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 习惯名称
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.habit_name)) },
                placeholder = { Text(stringResource(R.string.habit_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.nameError != null
            )
            
            if (uiState.nameError != null) {
                Text(
                    text = uiState.nameError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // 习惯描述
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text(stringResource(R.string.description_optional)) },
                placeholder = { Text(stringResource(R.string.description_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryIconSection(
    uiState: AddEditHabitUiState,
    viewModel: AddEditHabitViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.category_and_icon),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 分类选择
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    listOf("健康", "学习", "工作", "生活", "运动", "阅读").forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                viewModel.updateCategory(category)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            
            // 颜色选择
            Text(
                text = stringResource(R.string.habit_color),
                style = MaterialTheme.typography.bodyMedium
            )
            
            ColorPicker(
                selectedColor = uiState.color,
                onColorSelected = viewModel::updateColor
            )
        }
    }
}

@Composable
private fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#2196F3", "#4CAF50", "#FF9800", "#F44336",
        "#9C27B0", "#00BCD4", "#8BC34A", "#FF5722"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.forEach { color ->
            Card(
                modifier = Modifier
                    .size(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(
                        android.graphics.Color.parseColor(color)
                    )
                ),
                border = if (selectedColor == color) {
                    androidx.compose.foundation.BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    )
                } else null,
                onClick = { onColorSelected(color) }
            ) {}
        }
    }
}

@Composable
private fun FrequencySection(
    uiState: AddEditHabitUiState,
    viewModel: AddEditHabitViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.frequency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 频率类型选择
            val frequencyTypes = listOf(
                stringResource(R.string.daily),
                stringResource(R.string.weekly),
                stringResource(R.string.monthly)
            )
            
            frequencyTypes.forEachIndexed { index, type ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.frequencyType == index,
                        onClick = { viewModel.updateFrequencyType(index) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = type)
                }
            }
            
            // 目标次数（仅周频率和月频率）
            if (uiState.frequencyType > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.target_count),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.updateTargetCount(maxOf(1, uiState.targetCount - 1)) }
                        ) {
                            Text("-", style = MaterialTheme.typography.titleLarge)
                        }
                        
                        Text(
                            text = uiState.targetCount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        IconButton(
                            onClick = { viewModel.updateTargetCount(uiState.targetCount + 1) }
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderSection(
    uiState: AddEditHabitUiState,
    viewModel: AddEditHabitViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.reminder_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 启用提醒
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.enable_reminder))
                Switch(
                    checked = uiState.reminderEnabled,
                    onCheckedChange = viewModel::updateReminderEnabled
                )
            }
            
            // 提醒时间（仅当启用提醒时）
            if (uiState.reminderEnabled) {
                Text(
                    text = stringResource(R.string.reminder_time),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Card(
                    onClick = { /* TODO: 打开时间选择器 */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.reminderTime,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
} 