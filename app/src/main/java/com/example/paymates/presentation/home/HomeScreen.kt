package com.example.paymates.presentation.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.paymates.presentation.Navigation.PayMatesScreens
import com.example.paymates.presentation.createGroup.GroupViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.paymates.R
import com.example.paymates.domain.CreateGroupUiState
import com.example.paymates.ui.theme.Poppins
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Composable
fun HomeScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val viewModel: GroupViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadUserGroups(userId)
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = { TopAppBar(navController) },
        bottomBar = {
            BottomNavigation(navController)
        }
    ) { it ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it),){
            HomeScreenContent(navController)
        }

    }
}

@Composable
fun HomeScreenContent(navController: NavController) {
    val viewModel: GroupViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(currentUserId) {
        currentUserId?.let { viewModel.loadUserGroups(it) }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFECEFF1))
        ) {
            when (state) {
                is CreateGroupUiState.Loading -> CircularProgressIndicator()

                is CreateGroupUiState.GroupsLoaded -> {
                    val groups = (state as CreateGroupUiState.GroupsLoaded).groups

                    if (groups.isEmpty()) {
                        Text("No Groups Found", fontSize = 18.sp, color = Color.Gray)
                    } else {
                        LazyColumn {
                            items(groups, key = { it.groupId }) { group ->
                                GroupCard(
                                    profileImageUrl = group.profileImageUrl,
                                    groupName = group.name,
                                    senderName = group.lastMessage?.senderName ?: "",
                                    lastMessage = group.lastMessage?.message ?: "",
                                    timestamp = group.lastMessage?.timestamp,
                                    onClick = {
                                        navController.navigate("${PayMatesScreens.ChatScreen.name}/${group.groupId}")
                                    }
                                )
                            }
                        }
                    }
                }

                is CreateGroupUiState.Error -> {
                    Text(
                        "Error: ${(state as CreateGroupUiState.Error).message}",
                        color = Color.Red
                    )
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                navController.navigate(PayMatesScreens.ContactScreen.name)
                Log.d("ToContacts", "navigated successfully")
            }) {
                Text("To Contacts")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                navController.navigate(PayMatesScreens.CreateGroupScreen.name)
                Log.d("ToCreateGroup", "navigated successfully")
            }) {
                Text("To Create Group")
            }
        }
    }
}

@Composable
fun GroupCard(
    profileImageUrl: String?,
    groupName: String,
    senderName: String,
    lastMessage: String,
    timestamp: Long?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        //elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Group photo",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))


                Text(
                    text = groupName,
                    fontSize = 16.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Black
                )
            }
            //Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(16.dp),) {
                Text(
                    text = "${senderName.split(" ")[0]}: $lastMessage",
                    fontSize = 14.sp,
                    fontFamily = Poppins,
                    color = Color(0xFF607D8B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )


                Text(
                    text = if (timestamp != null && timestamp > 0) {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
                    } else {
                        ""
                    },
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF607D8B),
                )
            }
        }
        }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(navController: NavController) {
    TopAppBar(
        // modifier = Modifier.height(120.dp),
        title = {
            Image(
                painter = painterResource(id = R.drawable.logo), // Replace with your logo
                contentDescription = "Inpage Logo",
                modifier = Modifier.width(120.dp).height(34.dp),
                //contentScale = ContentScale.Crop
            )
        },
        actions = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp).size(24.dp),
                imageVector = Icons.Default.Notifications,
                contentDescription = "notifications",
                tint = Color(0xFF0A2540)
            )
        }
    )
}

@Composable
fun BottomNavigation(navController: NavController) {
    Surface(
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(77.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate(PayMatesScreens.HomeScreen.name)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home",
                    modifier = Modifier
                        .height(28.dp)
                        .width(28.dp),
                    tint = Color(0xFF607D8B)
                )
                Text(
                    text = "Home",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Poppins,
                    color = Color(0xFF607D8B)
                )
            }

            // New Scan (centered and elevated)
            Box(
                modifier = Modifier
                    //.offset(y = (-16).dp)
                    .background(Color(0xFF0A2540), shape = RoundedCornerShape(20.dp))
                    .clickable { navController.navigate(PayMatesScreens.CreateGroupScreen.name) }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("New Group", color = Color.White, fontSize = 12.sp , fontFamily = Poppins)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Profile
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate(PayMatesScreens.ProfileScreen.name)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .height(28.dp)
                        .width(28.dp),
                    tint = Color(0xFF607D8B)
                )
                Text(
                    text = "Profile",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Poppins,
                    color = Color(0xFF607D8B)
                )
            }
        }
    }
}