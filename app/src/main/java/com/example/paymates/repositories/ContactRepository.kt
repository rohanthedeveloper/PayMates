package com.example.paymates.repositories

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.example.paymates.data.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ContactRepository (
    private val context: Context,
    private val firestore: FirebaseFirestore
){
    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contactList = mutableListOf<Contact>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY
        )
        cursor?.use { contactsCursor ->
            val idIndex = contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoUriIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (contactsCursor.moveToNext()) {
                val uid = contactsCursor.getString(idIndex) ?: "N/A"
                val name = contactsCursor.getString(nameIndex) ?: "Unknown"
                val number = contactsCursor.getString(numberIndex)?.replace(" ", "") ?: "N/A"
                val photoUri = if (photoUriIndex != -1) contactsCursor.getString(photoUriIndex) else null

                contactList.add(Contact(uid, name, number, photoUri, false))
            }
        }
        contactList
    }
    suspend fun checkRegistrationStatus(contacts: List<Contact>): List<Contact> = withContext(Dispatchers.IO) {
        val updatedContacts = mutableListOf<Contact>()
        val userCollection = firestore.collection("users")

        contacts.forEach { contact ->
            val querySnapshot = userCollection
                .whereEqualTo("phoneNumber", contact.phoneNumber)
                .get()
                .await()

            updatedContacts.add(contact.copy(isRegistered = !querySnapshot.isEmpty))
        }

        updatedContacts
    }
}