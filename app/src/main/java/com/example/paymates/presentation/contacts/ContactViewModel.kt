package com.example.paymates.presentation.contacts

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paymates.data.Contact
import com.example.paymates.data.User
import com.example.paymates.domain.ContactUiState
import com.example.paymates.repositories.ContactRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ContactViewModel(private val repository: ContactRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    fun loadContacts() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, error = null) }

        try {
            val contacts = repository.getContacts()
            val registeredContacts = mutableListOf<Contact>()
            val unregisteredContacts = mutableListOf<Contact>()

            val userCollection = Firebase.firestore.collection("users")

            // Normalize all phone numbers (remove +91, spaces, dashes)
            val phoneNumbers = contacts.map { normalizePhoneNumber(it.phoneNumber) }
                .distinct()

            val registeredNumbers = mutableSetOf<String>()

            // Query Firestore in batches of 30
            phoneNumbers.chunked(30).forEach { batch ->
                val querySnapshot = userCollection.whereIn("phoneNumber", batch).get().await()
               // registeredNumbers.addAll(querySnapshot.documents.mapNotNull { it.getString("phoneNumber") })
                querySnapshot.documents.forEach { doc ->
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        val cleanedPhone = normalizePhoneNumber(user.phoneNumber)
                        if (registeredContacts.none { it.phoneNumber == cleanedPhone }) {
                            val matchingContact =
                                contacts.find { normalizePhoneNumber(it.phoneNumber) == cleanedPhone }
                            matchingContact?.let {
                                registeredContacts.add(
                                    Contact(
                                        uid = user.uid,
                                        name = it.name,
                                        phoneNumber = cleanedPhone,
                                        photoUri = it.photoUri,
                                        isRegistered = true
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Categorize contacts (Registered vs. Unregistered)
            contacts.forEach { contact ->
                val cleanedNumber = normalizePhoneNumber(contact.phoneNumber)

                if (cleanedNumber in registeredNumbers) {
                    if (registeredContacts.none { it.phoneNumber == cleanedNumber }) {
                        registeredContacts.add(contact.copy(phoneNumber = cleanedNumber, isRegistered = true))
                    }
                } else {
                    if (unregisteredContacts.none { it.phoneNumber == cleanedNumber }) {
                        unregisteredContacts.add(contact.copy(phoneNumber = cleanedNumber, isRegistered = false))
                    }
                }
            }

            // Sort and group contacts
            val sortedContacts = registeredContacts
                .groupBy { it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
                .toMutableMap()

            if (unregisteredContacts.isNotEmpty()) {
                sortedContacts["Invite Friends"] = unregisteredContacts
            }

            Log.d("ContactViewModel", "Registered: ${registeredContacts.size}, Unregistered: ${unregisteredContacts.size}")

            _uiState.update { it.copy(loading = false, contacts = sortedContacts) }
        } catch (e: Exception) {
            _uiState.update { it.copy(loading = false, error = e.localizedMessage) }
        }
    }

    // Function to normalize phone numbers
    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace("\\s".toRegex(), "")  // Remove spaces
            .replace("-", "") // Remove dashes
            .replace("(", "").replace(")", "") // Remove brackets
            .removePrefix("+91") // Remove +91
    }
}