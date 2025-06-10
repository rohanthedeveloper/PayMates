package com.example.paymates.presentation.splash

import android.window.SplashScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paymates.R
import com.example.paymates.data.User
import com.example.paymates.presentation.Navigation.PayMatesScreens
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun SplashScreen(navController: NavController) {
    val auth = Firebase.auth
    val db = Firebase.firestore

    var user by remember { mutableStateOf(auth.currentUser) }
    var userProfileChecked by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = user) {
        delay(2000L)

        if (user == null) {
            navController.navigate(PayMatesScreens.AuthScreen.name) {
                popUpTo(PayMatesScreens.SplashScreen.name) { inclusive = true }
            }
        } else {
            val userDoc = db.collection("users").document(user!!.uid).get().await()
            if (userDoc.exists()) {
                val userData = userDoc.toObject(User::class.java)
                if (userData?.name.isNullOrEmpty() || userData.photoUrl.isNullOrEmpty()) {
                    navController.navigate(PayMatesScreens.ProfileScreen.name) {
                        popUpTo(PayMatesScreens.SplashScreen.name) { inclusive = true }
                    }
                } else {
                    navController.navigate(PayMatesScreens.HomeScreen.name) {
                        popUpTo(PayMatesScreens.SplashScreen.name) { inclusive = true }
                    }
                }
            } else {
                navController.navigate(PayMatesScreens.ProfileScreen.name) {
                    popUpTo(PayMatesScreens.SplashScreen.name) { inclusive = true }
                }
            }
        }
    }

    Splash()
}

@Composable
fun Splash() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Paymates logo",
            contentScale = ContentScale.Crop,
            //modifier = Modifier.height(50.dp)
        )
    }
}