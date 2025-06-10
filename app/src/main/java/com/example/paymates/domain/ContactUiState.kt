package com.example.paymates.domain

import com.example.paymates.data.Contact
import java.util.Collections

data class ContactUiState(
    val loading: Boolean = false,
    val contacts: Map<String, List<Contact>> = emptyMap(),
    val error: String? = null
)