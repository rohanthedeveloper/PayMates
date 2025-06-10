package com.example.paymates.data

data class Message(
    val senderId: String = "",
    val message: String = "",
    val senderName: String = "",
    val timestamp: Long? = null
)