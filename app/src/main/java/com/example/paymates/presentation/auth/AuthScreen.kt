package com.example.paymates.presentation.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.paymates.domain.AuthState
import com.example.paymates.presentation.Navigation.PayMatesScreens
import org.koin.androidx.compose.koinViewModel
import android.app.AlertDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.paymates.R
import com.example.paymates.ui.theme.Poppins
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import kotlinx.coroutines.delay

@Composable
fun OtpInputField(
    otpLength: Int = 6,
    error: Boolean = false,
    onOtpComplete: (String) -> Unit
) {
    val focusRequesters = List(otpLength) { FocusRequester() }
    val otpValues = remember { mutableStateListOf(*Array(otpLength) { "" }) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until otpLength) {
            val value = otpValues[i]
            val borderColor by animateColorAsState(
                targetValue = if (error) Color.Red else Color.Gray,
                animationSpec = tween(durationMillis = 300)
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                    .background(Color(0xFFE9EEF2), RoundedCornerShape(8.dp))
                    .focusRequester(focusRequesters[i]),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { input ->
                        if (input.length <= 1 && input.all { it.isDigit() }) {
                            otpValues[i] = input
                            if (input.isNotEmpty() && i < otpLength - 1) {
                                focusRequesters[i + 1].requestFocus()
                            } else if (otpValues.all { it.length == 1 }) {
                                onOtpComplete(otpValues.joinToString(""))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF607D8B)
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .onKeyEvent {
                            if (it.key == Key.Backspace && value.isEmpty() && i > 0) {
                                otpValues[i - 1] = ""
                                focusRequesters[i - 1].requestFocus()
                                true
                            } else false
                        },
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            innerTextField()
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }
}

@Composable
fun ResendOtpTimer(
    initialSeconds: Int = 60,
    onResendClicked: () -> Unit
) {
    var secondsLeft by remember { mutableStateOf(initialSeconds) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    if (secondsLeft > 0) {
        Text(
            text = "Resend OTP in ${secondsLeft}s",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    } else {
        Text(
            text = "Resend OTP",
            color = Color(0xFF0A2540),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { onResendClicked() }
                //.padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("ContextCastToActivity")
@Composable
fun AuthScreen(viewModel: AuthViewModel = koinViewModel(), navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpError by remember { mutableStateOf(false) }
    var isCodeSent by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = Modifier.fillMaxSize().background(color = Color(0xFFECEFF1)),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(modifier = Modifier.height(120.dp))
        Text(
            text = "Welcome to",
            fontSize = 14.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium
        )
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.height(50.dp)
        )
        Spacer(modifier = Modifier.height(60.dp))

        AnimatedContent(
            targetState = isCodeSent,
            transitionSpec = {
                slideInHorizontally { fullWidth -> fullWidth } + fadeIn() with
                        slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut()
            },
            label = "AuthSwitcher"
        ) { codeSent ->
            if (!codeSent) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Enter your phone number to continue",
                        fontFamily = Poppins,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE9EEF2), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+91",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF607D8B)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Divider(
                            color = Color(0xFF607D8B),
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF607D8B),
                                fontSize = 16.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            activity?.let { nonNullActivity ->
                                viewModel.sendVerificationCode("+91$phoneNumber", nonNullActivity)
                            } ?: Log.e("PhoneAuth", "Activity is null")
                            isCodeSent = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A2540))
                    ) {
                        Text(
                            "Send OTP",
                            fontFamily = Poppins,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isCodeSent = false }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Phone Number", fontFamily = Poppins)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OtpInputField(
                        otpLength = 6,
                        error = isOtpError,
                        onOtpComplete = { enteredOtp ->
                            otp = enteredOtp
                            isOtpError = false
                            //viewModel.verifyCode(enteredOtp)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ResendOtpTimer {
                        activity?.let { viewModel.sendVerificationCode("+91$phoneNumber", it) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.verifyCode(otp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A2540))
                    ) {
                        Text(
                            "Verify OTP",
                            fontFamily = Poppins,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        when (uiState) {
            is AuthState.Loading -> SendingOtp()
            is AuthState.Success -> {
                LaunchedEffect(Unit) { viewModel.onAuthSuccess() }
            }

            is AuthState.UserExists -> {
                viewModel.checkUserAndNavigate(navController)
            }

            is AuthState.Error -> {
                isOtpError = true
                LaunchedEffect(uiState) { context.showErrorDialog("Something went wrong") }
            }

            else -> {}
        }
    }
}

@Composable
fun SendingOtp(){
    Row(horizontalArrangement = Arrangement.Center) {
        Text(
            "Sending OTP",
            fontSize = 14.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(8.dp))
        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp).size(25.dp))
    }
}

fun Context.showErrorDialog(message: String) {
    AlertDialog.Builder(this)
        .setTitle("Error")
        .setMessage(message)
        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        .setIcon(R.drawable.ic_launcher_foreground)
        .show()
}
