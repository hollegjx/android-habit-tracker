package com.example.cur_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.data.database.entities.AiCharacterEntity
import com.example.cur_app.data.repository.AiCharacterRepository
import com.example.cur_app.data.local.AiCharacterManager
import com.example.cur_app.data.local.SelectedAiCharacter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI角色选择界面的ViewModel
 */
@HiltViewModel
class AiCharacterSelectionViewModel @Inject constructor(
    private val aiCharacterRepository: AiCharacterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiCharacterSelectionUiState())
    val uiState: StateFlow<AiCharacterSelectionUiState> = _uiState.asStateFlow()

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            aiCharacterRepository.getAllActiveCharacters()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
                .collect { characters ->
                    _uiState.value = _uiState.value.copy(
                        characters = characters,
                        isLoading = false
                    )
                    // 角色加载完成后，加载选中的角色
                    if (characters.isNotEmpty()) {
                        loadSelectedCharacter()
                    }
                }
        }
    }

    private fun loadSelectedCharacter() {
        viewModelScope.launch {
            try {
                var selectedCharacter = aiCharacterRepository.getSelectedCharacter()
                
                // 如果没有选中的角色，选择第一个角色
                if (selectedCharacter == null && _uiState.value.characters.isNotEmpty()) {
                    val firstCharacter = _uiState.value.characters.first()
                    aiCharacterRepository.selectCharacter(firstCharacter.id)
                    selectedCharacter = firstCharacter
                }
                
                // 更新UI状态
                _uiState.value = _uiState.value.copy(selectedCharacter = selectedCharacter)
                
                // 同步更新AiCharacterManager
                selectedCharacter?.let { character ->
                    val uiModel = character.toUiModel()
                    AiCharacterManager.updateCurrentCharacter(
                        SelectedAiCharacter(
                            id = uiModel.id,
                            name = uiModel.name,
                            iconEmoji = uiModel.iconEmoji,
                            subtitle = uiModel.subtitle,
                            backgroundColor = uiModel.backgroundColor
                        )
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(error = exception.message)
            }
        }
    }

    fun selectCharacter(character: AiCharacterEntity) {
        viewModelScope.launch {
            try {
                aiCharacterRepository.selectCharacter(character.id)
                aiCharacterRepository.incrementUsage(character.id)
                _uiState.value = _uiState.value.copy(selectedCharacter = character)
                
                // 同步更新AiCharacterManager
                val uiModel = character.toUiModel()
                AiCharacterManager.updateCurrentCharacter(
                    SelectedAiCharacter(
                        id = uiModel.id,
                        name = uiModel.name,
                        iconEmoji = uiModel.iconEmoji,
                        subtitle = uiModel.subtitle,
                        backgroundColor = uiModel.backgroundColor
                    )
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(error = exception.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * AI角色选择界面的UI状态
 */
data class AiCharacterSelectionUiState(
    val characters: List<AiCharacterEntity> = emptyList(),
    val selectedCharacter: AiCharacterEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)