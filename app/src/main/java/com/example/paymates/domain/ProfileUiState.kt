package com.example.paymates.domain

import com.example.paymates.data.User

sealed class ProfileUiState{
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}