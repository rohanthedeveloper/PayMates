package com.example.paymates.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Group(
    val groupId: String = "",
    val name: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val profileImageUrl: String? = null,
    @get:Exclude @set:Exclude
    var lastMessage: Message? = null
)