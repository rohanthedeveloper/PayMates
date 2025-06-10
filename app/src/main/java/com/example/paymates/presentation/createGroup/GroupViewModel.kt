package com.example.paymates.presentation.createGroup

import androidx.lifecycle.ViewModel
import com.example.paymates.data.User
import com.example.paymates.domain.CreateGroupUiState
import com.example.paymates.repositories.CreateGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroupViewModel(private val repository: CreateGroupRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateGroupUiState>(CreateGroupUiState.Loading)
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    fun createGroup(groupName: String, createdBy: String, members: List<User>, profileImageUrl: String?) {
        repository.createGroup(groupName, createdBy, members, profileImageUrl, { groupId ->
            _uiState.value = CreateGroupUiState.Sucess("Group created: $groupId")
        }, { exception ->
            _uiState.value = CreateGroupUiState.Error(exception.localizedMessage ?: "Error creating group")
        })
    }

    fun loadUserGroups(userId: String) {
        repository.getUserGroups(userId, { groups ->
            _uiState.value = CreateGroupUiState.GroupsLoaded(groups)
        }, { exception ->
            _uiState.value = CreateGroupUiState.Error(exception.localizedMessage ?: "Error loading groups")
        })
    }
}
