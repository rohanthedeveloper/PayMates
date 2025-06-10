package com.example.paymates.presentation.chat

import com.example.paymates.data.Message
import com.example.paymates.data.Split

sealed class ChatItem {
    data class MessageItem(val message: Message) : ChatItem()
    data class SplitItem(val split: Split) : ChatItem()
}