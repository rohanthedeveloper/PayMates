package com.example.paymates.presentation.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paymates.data.Message
import com.example.paymates.data.Split
import com.example.paymates.data.User
import com.example.paymates.repositories.ChatRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _splits = MutableStateFlow<List<Split>>(emptyList())
    val splits: StateFlow<List<Split>> = _splits.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _chatItems = MutableStateFlow<List<ChatItem>>(emptyList())
    val chatItems: StateFlow<List<ChatItem>> = _chatItems

    private var currentMessages = emptyList<Message>()
    private var currentSplits = emptyList<Split>()

    fun sendMessage(groupId: String, message: String, senderId: String , senderName: String) {
        if (message.isNotEmpty()&& senderId.isNotEmpty()) {
            try {
                repository.sendMessage(groupId, message, senderId , senderName)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    fun submitSplit(
        groupId: String,
        senderId: String,
        senderName: String,
        amount: Double,
        description: String,
        selectedMembers: List<User>,
        splitType: String, // "equal" or "manual"
        manualSplits: Map<String, Double> = emptyMap(),
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (selectedMembers.isEmpty()) {
            onFailure("No members selected")
            return
        }

        val splitMap = mutableMapOf<String, Double>()
        val statusMap = mutableMapOf<String, String>()

        when (splitType) {
            "equal" -> {
                val perPerson = String.format("%.2f", amount / selectedMembers.size).toDouble()
                selectedMembers.forEach {
                    splitMap[it.uid] = perPerson
                    statusMap[it.uid] = "pending"
                }
            }

            "manual" -> {
                val total = manualSplits.values.sum()
                if (total != amount) {
                    onFailure("Manual split does not match total amount")
                    return
                }
                selectedMembers.forEach {
                    val amt = manualSplits[it.uid] ?: 0.0
                    splitMap[it.uid] = amt
                    statusMap[it.uid] = "pending"
                }
            }

            else -> {
                onFailure("Invalid split type")
                return
            }
        }

        val docRef = Firebase.firestore.collection("groups").document(groupId)
            .collection("splits").document()

        val split = Split(
            id = docRef.id,
            senderId = senderId,
            senderName = senderName,
            amount = amount,
            description = description,
            taggedMembers = selectedMembers.map { it.uid },
            splitType = splitType,
            timestamp = System.currentTimeMillis(),
            splits = splitMap,
            statusMap = statusMap
        )

        try {
            repository.sendSplit(groupId, split)
            onSuccess()
        } catch (e: Exception) {
            onFailure("Failed to send split: ${e.message}")
        }
    }
    fun loadMessages(groupId: String) {
        viewModelScope.launch {
            repository.getMessages(groupId).collect { messages ->
                _messages.value = messages
                currentMessages = messages
                updateChatItems()
            }
        }
    }

    fun loadSplits(groupId: String) {
        viewModelScope.launch {
            repository.getSplits(
                groupId,
                onUpdate = { splits ->
                    currentSplits = splits
                    _error.value = null
                    updateChatItems()
                },
                onError = { exception ->
                    _error.value = exception.localizedMessage
                }
            )
        }
    }

    private fun updateChatItems() {
        val merged = mutableListOf<ChatItem>()
        merged += currentMessages.map { ChatItem.MessageItem(it) }
        merged += currentSplits.map { ChatItem.SplitItem(it) }
        _chatItems.value = merged.sortedBy {
            when (it) {
                is ChatItem.MessageItem -> it.message.timestamp
                is ChatItem.SplitItem -> it.split.timestamp
            }
        }
    }

    fun markPaymentComplete(groupId: String, splitId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.markPaymentComplete(groupId, splitId, userId)
            } catch (e: Exception) {
                Log.e("SplitViewModel", "Error updating payment status", e)
            }
        }
    }
}
