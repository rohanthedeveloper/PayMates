package com.example.paymates.data

data class Contact(
    val uid: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String?,
    val isRegistered: Boolean = false
)
