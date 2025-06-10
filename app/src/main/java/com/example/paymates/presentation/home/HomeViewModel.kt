package com.example.paymates.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class HomeViewModel(private val db : FirebaseFirestore): AndroidViewModel(Application()) {

}