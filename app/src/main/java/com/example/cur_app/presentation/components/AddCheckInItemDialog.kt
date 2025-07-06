package com.example.cur_app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cur_app.data.local.entity.CheckInType

/**
 * 添加新打卡项目对话框
 * 提供创建新打卡项目的完整表单界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCheckInItemDialog(
    isVisible: Boolean,
    selectedType: CheckInType,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, targetValue: Int, unit: String, icon: String, color: String) -> Unit
) {
    if (!isVisible) return

    // 表单状态
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("📚") }
    var selectedColor by remember { mutableStateOf(selectedType.colorString) }

    // 表单验证
    val isTitleValid = title.isNotBlank() && title.length <= 50
    val isDescriptionValid = description.length <= 200
    val isTargetValueValid = targetValue.toIntOrNull()?.let { it > 0 && it <= 9999 } == true
    val isUnitValid = unit.isNotBlank() && unit.length <= 10
    val isFormValid = isTitleValid && isDescriptionValid && isTargetValueValid && isUnitValid

    // 预设选项
    val iconOptions = when (selectedType) {
        CheckInType.STUDY -> listOf("📚", "💻", "📖", "✏️", "🎓", "🔬", "📝", "🖥️")
        CheckInType.EXERCISE -> listOf("🏃", "💪", "🚴", "🏊", "🧘", "⚽", "🏋️", "🤸")
        CheckInType.MONEY -> listOf("💰", "📈", "💳", "🏦", "💵", "📊", "💎", "🪙")
    }

    val unitOptions = when (selectedType) {
        CheckInType.STUDY -> listOf("分钟", "小时", "页", "个", "章", "课")
        CheckInType.EXERCISE -> listOf("分钟", "千卡", "次", "组", "公里", "小时")
        CheckInType.MONEY -> listOf("元", "次", "份", "项", "笔", "单")
    }

    val colorOptions = listOf(
        selectedType.colorString,
        "#FF5722", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFC107", "#FF9800", "#FF5722", "#795548"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "新建${selectedType.displayName}项目",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(selectedType.colorString))
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }

                // 项目名称
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 50) title = it },
                    label = { Text("项目名称*") },
                    placeholder = { Text("例如：${getDefaultTitle(selectedType)}") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isTitleValid && title.isNotEmpty(),
                    supportingText = {
                        Text("${title.length}/50", textAlign = TextAlign.End)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )

                // 项目描述
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 200) description = it },
                    label = { Text("项目描述") },
                    placeholder = { Text("简要描述你的目标...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    supportingText = {
                        Text("${description.length}/200", textAlign = TextAlign.End)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )

                // 目标值和单位（同一行）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 目标值
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 4) targetValue = it },
                        label = { Text("目标值*") },
                        placeholder = { Text("30") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !isTargetValueValid && targetValue.isNotEmpty(),
                        leadingIcon = {
                            Icon(Icons.Default.Star, contentDescription = null)
                        }
                    )

                    // 单位选择
                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("单位*") },
                            placeholder = { Text("选择单位") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            unitOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        unit = option
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 图标选择
                Column {
                    Text(
                        text = "选择图标",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(iconOptions.size) { index ->
                            val icon = iconOptions[index]
                            Card(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { selectedIcon = icon },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedIcon == icon) 
                                        Color(android.graphics.Color.parseColor(selectedType.colorString)).copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (selectedIcon == icon) 
                                    BorderStroke(2.dp, Color(android.graphics.Color.parseColor(selectedType.colorString)))
                                else null
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = icon,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 颜色选择
                Column {
                    Text(
                        text = "选择颜色",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(colorOptions.size) { index ->
                            val color = colorOptions[index]
                            Card(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { selectedColor = color },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(android.graphics.Color.parseColor(color))
                                ),
                                border = if (selectedColor == color) 
                                    BorderStroke(3.dp, MaterialTheme.colorScheme.outline)
                                else null
                            ) {
                                if (selectedColor == color) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            if (isFormValid) {
                                onConfirm(
                                    title.trim(),
                                    description.trim(),
                                    targetValue.toInt(),
                                    unit,
                                    selectedIcon,
                                    selectedColor
                                )
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(android.graphics.Color.parseColor(selectedType.colorString))
                        )
                    ) {
                        Text("创建")
                    }
                }
            }
        }
    }
}

/**
 * 获取默认标题示例
 */
private fun getDefaultTitle(type: CheckInType): String {
    return when (type) {
        CheckInType.STUDY -> "英语单词背诵"
        CheckInType.EXERCISE -> "晨跑锻炼"
        CheckInType.MONEY -> "每日储蓄"
    }
}

 