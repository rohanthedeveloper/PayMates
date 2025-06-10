package com.example.paymates.di

import com.example.paymates.presentation.chat.ChatViewModel
import com.example.paymates.presentation.GroupDetailScreen.GroupDetailViewModel
import com.example.paymates.presentation.auth.AuthViewModel
import com.example.paymates.presentation.contacts.ContactViewModel
import com.example.paymates.presentation.createGroup.GroupViewModel
import com.example.paymates.presentation.profile.ProfileViewModel
import com.example.paymates.repositories.ChatRepository
import com.example.paymates.repositories.ContactRepository
import com.example.paymates.repositories.CreateGroupRepository
import com.example.paymates.repositories.GroupDetailRepository
import com.example.paymates.repositories.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { ContactRepository(get() , get()) }
    single { CreateGroupRepository(get() , get()) }
    single { GroupDetailRepository(get()) }
    single { ChatRepository(get()) }
    single { ProfileRepository(get()) }
    viewModel { ContactViewModel(get()) }
    viewModel{AuthViewModel(get() , get())}
    viewModel{ GroupViewModel(get()) }
    viewModel{ GroupDetailViewModel(get()) }
    viewModel{ ChatViewModel(get()) }
    viewModel{ ProfileViewModel(get()) }
}