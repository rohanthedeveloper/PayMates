package com.example.paymates.presentation.GroupDetailScreen


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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.paymates.data.User
import com.example.paymates.domain.GroupDetailUiState
import com.example.paymates.presentation.Navigation.PayMatesScreens
import com.example.paymates.presentation.home.TopAppBar
import com.example.paymates.ui.theme.Poppins
import org.koin.androidx.compose.koinViewModel


@Composable
fun GroupDetailScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(groupId) {
        viewModel.loadGroupDetails(groupId)
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding().fillMaxSize(),
        topBar = {
            when(val state = uiState){
                is GroupDetailUiState.GroupLoaded -> {
                    GroupDetailTopAppBar(
                        navController = navController,
                        groupName = state.group.name,
                        photoUrl = state.group.profileImageUrl,
                        memberCount = state.members.size,
                        onClick = {

                        }
                    )
                }
                is GroupDetailUiState.Loading -> {

                }
                is GroupDetailUiState.Error -> {

                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GroupDetailScreenContent(
               // modifier = Modifier.padding(paddingValues),
                onAddClick = { navController.navigate("${PayMatesScreens.AddMemberScreen.name}/$groupId") },
                onEditClick = {},
                onExitClick = {},
                uiState = uiState
            )
        }
    }
}

@Composable
fun GroupDetailTopAppBar(navController: NavController , groupName: String? , photoUrl: String?, memberCount: Int , onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White)
            .clickable(onClick = onClick),
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
        Row(modifier = Modifier.align(alignment = Alignment.Center)) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "profile photo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = groupName.toString(), fontFamily = Poppins , fontWeight = FontWeight.Bold , fontSize = 16.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "$memberCount members" , fontFamily = Poppins , fontWeight = FontWeight.Bold , fontSize = 12.sp , color = Color(0xFF607D8B))
            }
        }
    }
}

@Composable
fun GroupDetailScreenContent(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onEditClick: () -> Unit,
    onExitClick: () -> Unit,
    uiState: GroupDetailUiState
){
    Column(modifier = Modifier.background(color = Color(0xFFECEFF1))) {
        Spacer(modifier = Modifier.height(16.dp))

        Column() {
            when (uiState) {
                is GroupDetailUiState.Loading -> CircularProgressIndicator()

                is GroupDetailUiState.GroupLoaded -> {
                    val groupState = uiState
                    val group = groupState.group
                    val users = groupState.members

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth() , horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(modifier = Modifier.background(color = Color(0xFFFFFFFF)).clickable(onClick = onAddClick).clip(RoundedCornerShape(12.dp)) , contentAlignment = Alignment.Center){
                            Row(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Add Member", fontSize = 14.sp , fontWeight = FontWeight.SemiBold , fontFamily = Poppins, color = Color(0xFF607D8B))
                                Icon(imageVector = Icons.Default.Add , contentDescription = "",tint = Color(0xFF607D8B) , modifier = Modifier.size(20.dp))
                            }
                        }
                        Box(modifier = Modifier.background(color = Color(0xFFFFFFFF)).clickable(onClick = onEditClick).clip(RoundedCornerShape(12.dp)) , contentAlignment = Alignment.Center){
                            Row(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Edit", fontSize = 14.sp , fontWeight = FontWeight.SemiBold , fontFamily = Poppins , color = Color(0xFF607D8B))
                                Icon(imageVector = Icons.Default.Edit , contentDescription = "",tint = Color(0xFF607D8B),modifier = Modifier.size(20.dp))
                            }
                        }
                        Box(modifier = Modifier.background(color = Color(0xFFFFFFFF)).clickable(onClick = onExitClick).clip(RoundedCornerShape(12.dp)) , contentAlignment = Alignment.Center){
                            Row(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Exit Group", fontSize = 14.sp , fontWeight = FontWeight.SemiBold , fontFamily = Poppins, color = Color(0xFFEB3223))
                                Icon(imageVector = Icons.Default.ExitToApp , contentDescription = "",tint = Color(0xFFEB3223),modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    Text(
                        text = "Group Members:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Poppins
                    )

                    if (users.isEmpty()) {
                        Text("No members yet.", fontSize = 14.sp, color = Color.Gray)
                    } else {
                        LazyColumn {
                            items(users) { user ->
                                MemberListItem(user)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                }

                is GroupDetailUiState.Error -> Text(
                    "Error: ${uiState.message}",
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun MemberListItem(member: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(color = Color(0xFFFFFFFF)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = member.photoUrl,
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(text = member.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = member.phoneNumber, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

