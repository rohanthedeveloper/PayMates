package com.example.paymates.repositories

import com.example.paymates.data.Group
import com.example.paymates.data.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CountDownLatch
import android.util.Log


class GroupDetailRepository(private val firestore: FirebaseFirestore) {

    fun getGroupDetails(groupId: String, onSuccess: (Group, List<User>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { document ->
                val group = document.toObject(Group::class.java)
                if (group != null) {
                    fetchMemberDetails(group.members, { users -> onSuccess(group, users) }, onFailure)
                } else {
                    onFailure(Exception("Group not found"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    private fun fetchMemberDetails(memberUids: List<String>, onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
        if (memberUids.isEmpty()) {
            onSuccess(emptyList())
            return
        }

        firestore.collection("users")
            .whereIn("uid", memberUids)
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { it.toObject(User::class.java) }
                onSuccess(users)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun addMember(groupId: String, memberUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("AddMember", "Fetching user with UID: $memberUid")
        firestore.collection("users")
            .document(memberUid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                if (user != null) {
                    Log.d("AddMember", "User found: ${user.uid}")
                    firestore.collection("groups").document(groupId)
                        .update("members", FieldValue.arrayUnion(user.uid))
                        .addOnSuccessListener {
                            Log.d("AddMember", "User added to group successfully")
                            onSuccess()
                        }
                        .addOnFailureListener {
                            Log.e("AddMember", "Failed to update group", it)
                            onFailure(it)
                        }
                } else {
                    Log.e("AddMember", "User not found")
                    onFailure(Exception("User not found"))
                }
            }
            .addOnFailureListener {
                Log.e("AddMember", "Failed to fetch user", it)
                onFailure(it)
            }
    }
}
