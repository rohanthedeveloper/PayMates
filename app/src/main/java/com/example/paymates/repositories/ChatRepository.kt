package com.example.paymates.repositories

import android.util.Log
import com.example.paymates.data.Message
import com.example.paymates.data.Split
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ChatRepository(private val db: FirebaseFirestore) {

    fun getMessages(groupId: String): Flow<List<Message>> = callbackFlow {
        val listener = db.collection("groups")
            .document(groupId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Correct order in Firestore
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val senderId = doc.getString("senderId") ?: ""
                    val message = doc.getString("message") ?: ""
                    val senderName = doc.getString("senderName") ?: ""
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time
                    Message(senderId, message, timestamp =  timestamp , senderName = senderName)
                }?.reversed() ?: emptyList() // REVERSE the list here

                trySend(messages).isSuccess
            }

        awaitClose { listener.remove() }
    }

    fun getSplits(
        groupId: String,
        onUpdate: (List<Split>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("groups").document(groupId)
            .collection("splits")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    onError(exception)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val splits = snapshot.documents.mapNotNull { it.toObject(Split::class.java) }
                    onUpdate(splits)
                }
            }
    }


    fun sendMessage(groupId: String, message: String, senderId: String, senderName: String) {
        if (message.isNotEmpty()) {
            val msgData = mapOf(
                "senderId" to senderId,
                "message" to message,
                "senderName" to senderName,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            db.collection("groups").document(groupId).collection("messages")
                .add(msgData)
                .addOnSuccessListener { documentReference ->
                    Log.d("Firestore", "Message sent successfully: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error sending message", e)
                }
        }
    }

    fun sendSplit(groupId: String, split: Split){

        val docRef = db.collection("groups").document(groupId)
            .collection("splits").document()

        val splitid = split.copy(id = docRef.id)

        val splitData = mapOf(
            "id" to splitid.id,
            "senderId" to splitid.senderId,
            "senderName" to splitid.senderName,
            "amount" to splitid.amount,
            "description" to splitid.description,
            "taggedMembers" to splitid.taggedMembers,
            "splitType" to splitid.splitType,
            "timestamp" to (splitid.timestamp ?: System.currentTimeMillis()),
            "splits" to splitid.splits,
            "statusMap" to splitid.statusMap
        )

        docRef.set(splitData)  // Use set() with the generated ID
            .addOnSuccessListener {
                Log.d("Firestore", "Split sent successfully: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error sending split", e)
            }
    }
    fun markPaymentComplete(groupId: String, splitId: String, userId: String) {
        val splitRef = db.collection("groups")
            .document(groupId)
            .collection("splits")
            .document(splitId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(splitRef)
            val currentStatusMap = snapshot.get("statusMap") as? Map<String, String> ?: emptyMap()

            val updatedStatusMap = currentStatusMap.toMutableMap()
            updatedStatusMap[userId] = "completed"

            transaction.update(splitRef, "statusMap", updatedStatusMap)
        }.addOnSuccessListener {
            Log.d("Firestore", "Payment status updated")
        }.addOnFailureListener {
            Log.e("Firestore", "Error updating payment status", it)
        }
    }


}

