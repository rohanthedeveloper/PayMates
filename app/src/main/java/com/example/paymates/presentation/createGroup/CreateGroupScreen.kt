package com.example.paymates.presentation.createGroup

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.paymates.R
import com.example.paymates.cloudinary.CloudinaryHelper
import com.example.paymates.data.User
import com.example.paymates.domain.CreateGroupUiState
import com.example.paymates.ui.theme.Poppins
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



@Composable
fun CreateGroupScreen(viewModel: GroupViewModel, navController: NavController) {
    var groupName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = { CreateGroupTopAppBar(navController) },
        containerColor = Color(0xFFF1F3F6) // Light grey background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Image
            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Selected Group Image",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            } ?: Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Optional fallback
                contentDescription = "Default Group Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Select Photo Text
            Text(
                text = "select profile photo",
                color = Color(0xFF007AFF), // iOS-style blue
                fontSize = 14.sp,
                modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Group Name Input
            TextField(
                value = groupName,
                onValueChange = { groupName = it },
                placeholder = { Text("Group Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Create Group Button
            Button(
                onClick = {
                    if (currentUserId != null && groupName.isNotBlank()) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(currentUserId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val user = document.toObject(User::class.java) ?: return@addOnSuccessListener

                                    if (imageUri != null) {
                                        isUploading = true
                                        val file = CloudinaryHelper.uriToFile(context, imageUri!!)
                                        if (file != null) {
                                            CloudinaryHelper.uploadToCloudinary(file) { imageUrl ->
                                                isUploading = false
                                                if (imageUrl != null) {
                                                    viewModel.createGroup(
                                                        groupName,
                                                        currentUserId,
                                                        listOf(user),
                                                        imageUrl
                                                    )
                                                } else {
                                                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Invalid image file", Toast.LENGTH_SHORT).show()
                                            isUploading = false
                                        }
                                    } else {
                                        viewModel.createGroup(groupName, currentUserId, listOf(user), null)
                                    }
                                } else {
                                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error fetching user details", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A2540)), // Navy blue
                enabled = !isUploading
            ) {
                if (isUploading)
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                else Text("Create Group", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State Handling
            when (state) {
                is CreateGroupUiState.Loading -> CircularProgressIndicator()
                is CreateGroupUiState.Sucess -> Toast.makeText(
                    context,
                    (state as CreateGroupUiState.Sucess).message,
                    Toast.LENGTH_SHORT
                ).show()

                is CreateGroupUiState.Error -> Text(
                    "Error: ${(state as CreateGroupUiState.Error).message}",
                    color = Color.Red
                )

                else -> {}
            }
        }
    }
}

    @Composable
    fun CreateGroupTopAppBar(navController: NavController) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White),
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            // Title
            Text(
                text = "Create Group",
                fontFamily = Poppins,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }
}
