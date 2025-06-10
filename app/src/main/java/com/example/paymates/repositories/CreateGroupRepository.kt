package com.example.paymates.repositories

import android.content.Context
import android.util.Log
import android.util.Log.e
import com.example.paymates.data.Group
import com.example.paymates.data.Message
import com.example.paymates.data.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query


class CreateGroupRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore
) {
    fun createGroup(name: String, createdBy: String, members: List<User>,profileImageUrl: String?, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val groupId = firestore.collection("groups").document().id

        val newGroup = hashMapOf(
            "groupId" to groupId,
            "name" to name,
            "createdBy" to createdBy,
            "members" to members.map { it.uid },  // 🔥 Store only user IDs
            "createdAt" to FieldValue.serverTimestamp(),
            "profileImageUrl" to profileImageUrl
        )

        firestore.collection("groups").document(groupId).set(newGroup)
            .addOnSuccessListener { onSuccess(groupId) }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUserGroups(
        userId: String,
        onSuccess: (List<Group>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("groups")
            .whereArrayContains("members", userId)
            .get()
            .addOnSuccessListener { groupResult ->
                val groups = mutableListOf<Group>()
                val groupDocs = groupResult.documents
                var processedCount = 0

                if (groupDocs.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                groupDocs.forEach { doc ->
                    val groupId = doc.id
                    val name = doc.getString("name") ?: ""
                    val createdBy = doc.getString("createdBy") ?: ""
                    val members = doc.get("members") as? List<String> ?: emptyList()
                    val createdAt = doc.getTimestamp("createdAt")
                    val profileImageUrl = doc.getString("profileImageUrl")

                    firestore.collection("groups")
                        .document(groupId)
                        .collection("messages")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { messageSnapshot ->
                            val messageDoc = messageSnapshot.documents.firstOrNull()
                            val lastMessage = messageDoc?.let {
                                try {
                                    Message(
                                        senderId = it.getString("senderId") ?: "",
                                        senderName = it.getString("senderName") ?: "",
                                        message = it.getString("message") ?: "",
                                        timestamp = it.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                                    )
                                } catch (e: Exception) {
                                    Log.e("FirestoreError", "Error parsing message: ${e.message}", e)
                                    null
                                }
                            }

                            val group = Group(
                                groupId = groupId,
                                name = name,
                                createdBy = createdBy,
                                members = members,
                                createdAt = createdAt,
                                profileImageUrl = profileImageUrl,
                                lastMessage = lastMessage
                            )
                            groups.add(group)
                            processedCount++
                            if (processedCount == groupDocs.size) {
                                onSuccess(groups.sortedByDescending { it.lastMessage?.timestamp ?: 0L })
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Failed to get last message: ${e.message}", e)
                            // Still add the group without message
                            val group = Group(
                                groupId = groupId,
                                name = name,
                                createdBy = createdBy,
                                members = members,
                                createdAt = createdAt,
                                profileImageUrl = profileImageUrl,
                                lastMessage = null
                            )
                            groups.add(group)
                            processedCount++
                            if (processedCount == groupDocs.size) {
                                onSuccess(groups.sortedByDescending { it.lastMessage?.timestamp ?: 0L })
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to fetch groups: ${e.message}", e)
                onFailure(e)
            }
    }


}
