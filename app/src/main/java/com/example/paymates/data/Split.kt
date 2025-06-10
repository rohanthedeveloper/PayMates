package com.example.paymates.data

data class Split(
    val id: String = "",
    val senderId: String = "",
    val senderName: String ="",
    val amount: Double = 0.0,
    val description: String = "",
    val taggedMembers: List<String> = emptyList(), //storing only uids of the users and not the whole users
    val splitType: String = "equal", // equal , custom
    val timestamp: Long? = null,
    val splits: Map<String , Double> = emptyMap(),
    val statusMap: Map<String , String> = emptyMap()
)

data class SplitUserUi(
    val uid: String,
    val name: String,
    val imageUrl: String,
    val isSelected: Boolean = false,
    val amount: String = "0"
)

data class SplitInputUiState(
    val amount: String = "",
    val description: String = "",
    val users: List<SplitUserUi> = emptyList(),
    val isEqualSplit: Boolean = false
)