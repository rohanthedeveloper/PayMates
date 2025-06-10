package com.example.paymates.presentation.auth

import android.R.attr.phoneNumber
import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.paymates.data.User
import com.example.paymates.domain.AuthState
import com.example.paymates.presentation.Navigation.PayMatesScreens
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthViewModel(private val auth : FirebaseAuth , private val db : FirebaseFirestore) : AndroidViewModel(Application()){
    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    private var storedVerificationId: String? = null
    private lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken

    private val callBacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _uiState.value = AuthState.Error("Verification Failed")
        }

        override fun onCodeSent(verifcationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            storedVerificationId = verifcationId
            resendingToken = token
            _uiState.value = AuthState.CodeSent
        }
    }

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity) // Pass Application context
            .setCallbacks(callBacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        _uiState.value = AuthState.Loading
    }

    fun verifyCode(code: String) {
        storedVerificationId?.let {
            val credential = PhoneAuthProvider.getCredential(it, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = AuthState.Success(auth.currentUser)
                } else {
                    _uiState.value = AuthState.Error("Login Failed")
                }
            }
        }
    }

    fun saveUserToFirestore(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val firebaseUser = auth.currentUser
        firebaseUser?.let { user ->
            val normalizedPhone = normalizePhoneNumber(user.phoneNumber ?: "")

            val userRef = db.collection("users").document(user.uid)
            userRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    // User doesn't exist, create new entry
                    val newUser = User(
                        uid = user.uid,
                        name = "", // You can ask user for the name later
                        phoneNumber = normalizedPhone, // Store normalized number
                        photoUrl = null
                    )
                    userRef.set(newUser)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception -> onFailure(exception) }
                } else {
                    // User already exists, update the phone number if needed
                    val existingPhone = document.getString("phoneNumber") ?: ""
                    if (existingPhone != normalizedPhone) {
                        userRef.update("phoneNumber", normalizedPhone)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { exception -> onFailure(exception) }
                    } else {
                        onSuccess()
                    }
                }
            }.addOnFailureListener { exception -> onFailure(exception) }
        } ?: onFailure(Exception("User is null"))
    }


    fun onAuthSuccess() {
        saveUserToFirestore(
            onSuccess = { _uiState.value = AuthState.UserExists },
            onFailure = { _uiState.value = AuthState.Error("Failed to save user") }
        )
    }

    fun checkUserAndNavigate(navController: NavController) {
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(auth.currentUser?.uid ?: "").get().await()
                if (userDoc.exists()) {
                    val user = userDoc.toObject(User::class.java)
                    if (user?.name.isNullOrEmpty()) {
                        navController.navigate(PayMatesScreens.ProfileScreen.name)
                    } else {
                        navController.navigate(PayMatesScreens.HomeScreen.name)
                    }
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error checking user", e)
            }
        }
    }
    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace("\\s".toRegex(), "")  // Remove spaces
            .replace("-", "") // Remove dashes
            .replace("(", "").replace(")", "") // Remove brackets
            .removePrefix("+91") // Remove +91 (for India)
    }
}