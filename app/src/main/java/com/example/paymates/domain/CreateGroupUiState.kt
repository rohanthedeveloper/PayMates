package com.example.paymates.domain

import com.example.paymates.data.Group

sealed class CreateGroupUiState {
    object Loading: CreateGroupUiState()
    data class Sucess(val message: String): CreateGroupUiState()
    data class Error(val message: String): CreateGroupUiState()
    data class GroupsLoaded(val groups: List<Group>): CreateGroupUiState()

}