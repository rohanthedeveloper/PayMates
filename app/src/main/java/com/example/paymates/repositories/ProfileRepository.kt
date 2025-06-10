package com.example.paymates.repositories

import com.example.paymates.data.User
import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepository(private val firestore: FirebaseFirestore){
    fun getUser(uid: String, onSuccess : (User) -> Unit, onFailure : (Exception) -> Unit) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                if (user != null) onSuccess(user) else onFailure(Exception("User not found"))
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateProfile(uid : String , name: String , photoUrl: String , onSuccess: () -> Unit , onFailure: (Exception) -> Unit){
        val data = mapOf(
            "name" to name,
            "photoUrl" to photoUrl
        )
        firestore.collection("users").document(uid).update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}