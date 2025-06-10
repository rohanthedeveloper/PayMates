package com.example.paymates.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paymates.data.User
import com.example.paymates.domain.ProfileUiState
import com.example.paymates.repositories.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState

    fun fetchUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        _profileUiState.value = ProfileUiState.Loading

        viewModelScope.launch {
            repository.getUser(uid,
                onSuccess = { userData ->
                    _profileUiState.value = ProfileUiState.Success(userData)
                },
                onFailure = { error ->
                    _profileUiState.value = ProfileUiState.Error(error.toString())
                }
            )
        }
    }

    fun updateUserProfile(name: String, photoUrl: String?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        _profileUiState.value = ProfileUiState.Loading

        viewModelScope.launch {
            repository.updateProfile(
                uid = uid,
                name = name,
                photoUrl = photoUrl.toString(),
                onSuccess = {
                    _profileUiState.value = ProfileUiState.Success(User(uid, name,
                        photoUrl.toString()
                    ))
                },
                onFailure = { error ->
                    _profileUiState.value = ProfileUiState.Error(error.toString())
                }
            )
        }
    }
}