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
import com.example.cur_app.data.database.entities.CheckInItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCheckInItemDialog(
    isVisible: Boolean,
    item: CheckInItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, targetValue: Int, unit: String, icon: String, color: String) -> Unit
) {
    if (!isVisible) return

    val selectedType = CheckInType.fromString(item.type)
    var title by remember(item) { mutableStateOf(item.title) }
    var description by remember(item) { mutableStateOf(item.description) }
    var targetValue by remember(item) { mutableStateOf(item.targetValue.toString()) }
    var unit by remember(item) { mutableStateOf(item.unit) }
    var selectedIcon by remember(item) { mutableStateOf(item.icon) }
    var selectedColor by remember(item) { mutableStateOf(item.color) }

    val isTitleValid = title.isNotBlank() && title.length <= 50
    val isDescriptionValid = description.length <= 200
    val isTargetValueValid = targetValue.toIntOrNull()?.let { it >= 30 && it <= 9999 } == true
    val isUnitValid = unit.isNotBlank() && unit.length <= 10
    val isFormValid = isTitleValid && isDescriptionValid && isTargetValueValid && isUnitValid

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
            modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "编辑${selectedType.displayName}项目",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(selectedType.colorString))
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 50) title = it },
                    label = { Text("项目名称*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isTitleValid && title.isNotEmpty(),
                    supportingText = { Text("${title.length}/50", textAlign = TextAlign.End) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 200) description = it },
                    label = { Text("项目描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    supportingText = { Text("${description.length}/200", textAlign = TextAlign.End) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 4) targetValue = it },
                        label = { Text("目标值*") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !isTargetValueValid && targetValue.isNotEmpty(),
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
                    )

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
                                    onClick = { unit = option; unitExpanded = false }
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(android.graphics.Color.parseColor(selectedType.colorString)).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(android.graphics.Color.parseColor(selectedType.colorString))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("完成奖励", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            text = "+${item.experienceValue} 经验值",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(android.graphics.Color.parseColor(selectedType.colorString))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("取消") }
                    
                    Button(
                        onClick = {
                            if (isFormValid) {
                                onConfirm(title.trim(), description.trim(), targetValue.toInt(), unit, selectedIcon, selectedColor)
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(android.graphics.Color.parseColor(selectedType.colorString))
                        )
                    ) { Text("保存") }
                }
            }
        }
    }
}