package com.example.paymates.domain

import com.example.paymates.data.Group
import com.example.paymates.data.User

sealed class GroupDetailUiState {
    object Loading : GroupDetailUiState()
    data class GroupLoaded(val group: Group , val members: List<User>) : GroupDetailUiState()
    data class Error(val message: String) : GroupDetailUiState()
}