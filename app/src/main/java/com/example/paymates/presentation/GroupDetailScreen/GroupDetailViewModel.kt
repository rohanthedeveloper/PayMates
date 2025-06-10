package com.example.paymates.presentation.GroupDetailScreen

import androidx.lifecycle.ViewModel
import com.example.paymates.domain.GroupDetailUiState
import com.example.paymates.repositories.GroupDetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroupDetailViewModel(private val repository: GroupDetailRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<GroupDetailUiState>(GroupDetailUiState.Loading)
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    fun loadGroupDetails(groupId: String) {
        _uiState.value = GroupDetailUiState.Loading
        repository.getGroupDetails(groupId, { group, users ->
            _uiState.value = GroupDetailUiState.GroupLoaded(group, users)
        }, { exception ->
            _uiState.value = GroupDetailUiState.Error(exception.localizedMessage ?: "Error loading group")
        })
    }

    fun addMemberToGroup(groupId: String, memberUid: String, onSuccess: () -> Unit) {
        repository.addMember(groupId, memberUid, {
            onSuccess()
        }, { exception ->
            _uiState.value = GroupDetailUiState.Error(exception.localizedMessage ?: "Error adding member")
        })
    }
}
