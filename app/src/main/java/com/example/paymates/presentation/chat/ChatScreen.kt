package com.example.paymates.presentation.chat

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.paymates.presentation.Navigation.PayMatesScreens
import org.koin.androidx.compose.koinViewModel
import com.example.paymates.data.Message
import com.example.paymates.data.Split
import com.example.paymates.data.User
import com.example.paymates.domain.GroupDetailUiState
import com.example.paymates.presentation.GroupDetailScreen.GroupDetailViewModel
import com.example.paymates.ui.theme.Nunito
import com.example.paymates.ui.theme.Poppins
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController , groupId: String , viewModel: ChatViewModel = koinViewModel(),groupDetailViewModel: GroupDetailViewModel = koinViewModel()){
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var currentUserName: String? = ""
    val uiState by groupDetailViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        if (groupId.isNotEmpty()) {
            groupDetailViewModel.loadGroupDetails(groupId)
        } else {
            Log.e("ChatScreen", "Invalid Group ID")
        }
    }

    if (true) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentUserName = document.getString("name")
                } else {
                    Log.d("UserName", "No such user document")
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserName", "Error fetching user name", e)
            }
    }

    LaunchedEffect(Unit) {
        if (groupId.isNotEmpty()) {
            viewModel.loadMessages(groupId)
            viewModel.loadSplits(groupId)
        } else {
            Log.e("ChatScreen", "Invalid Group ID")
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize().systemBarsPadding(),
        topBar = {
            when (val state = uiState) {
                is GroupDetailUiState.GroupLoaded -> {
                    ChatScreenTopAppBar(
                        navController = navController,
                        groupName = state.group.name,
                        photoUrl = state.group.profileImageUrl,
                        memberCount = state.members.size,
                        onClick = {
                            navController.navigate("${PayMatesScreens.GroupDetailScreen.name}/$groupId")
                        }
                    )
                }
                is GroupDetailUiState.Loading -> {
                    TopAppBar(title = { Text("Loading...") })
                }
                is GroupDetailUiState.Error -> {
                    TopAppBar(title = { Text("Error loading group") })
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            ChatScreenContent(
               // modifier = Modifier.padding(paddingValues),
                viewModel = viewModel,
                navController = navController,
                groupId = groupId,
                currentUserName = currentUserName
            )
        }
    }
}

@Composable
fun ChatScreenContent(modifier: Modifier = Modifier , viewModel: ChatViewModel , navController: NavController, groupId: String,currentUserName: String?) {
    val messages by viewModel.messages.collectAsState()
    val splits by viewModel.splits.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val chatItems by viewModel.chatItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(color = Color(0xFFECEFF1)).imePadding()) {
//        LazyColumn(
//            modifier = Modifier.weight(1f),
//            reverseLayout = true
//        ) {
//            items(messages) { message ->
//                ChatMessageItem(message, currentUserId)
//            }
//        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = false
        ) {
            items(chatItems) { item ->
                when (item) {
                    is ChatItem.MessageItem -> ChatMessageItem(item.message , currentUserId)
                    is ChatItem.SplitItem -> {
                        var showDialog by remember { mutableStateOf(false) }
                        if (showDialog) {
                            SplitPopUpDialog(
                                split = item.split,
                                currentUserId = currentUserId,
                                onDismissClick = { showDialog = false },
                                viewModel = viewModel,
                                groupId = groupId
                            )
                        }
                        SplitCard(item.split , currentUserId , onClick = {showDialog = true}, viewModel , groupId = groupId)}
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Field
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = {
                    Text(
                        text = "Add a message",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF607D8B),
                        fontSize = 14.sp,
                    )
                },
                textStyle = TextStyle(
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF607D8B)
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .background(Color.White, RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 3,
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Icon Button
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(
                            groupId, messageText, currentUserId,
                            currentUserName.toString()
                        )
                        messageText = ""
                    } else {
                        val encodedName = Uri.encode(currentUserName ?: "")
                        navController.navigate("${PayMatesScreens.SplitExpenseScreen.name}/$groupId/$currentUserId/$encodedName")                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = if (messageText.isNotBlank()) Icons.Default.Send else Icons.Default.Add,
                    contentDescription = if (messageText.isNotBlank()) "Send" else "Add",
                    tint = Color(0xFF607D8B)
                )
            }
        }
    }
}

@Composable
fun ChatScreenTopAppBar(navController: NavController , groupName: String? , photoUrl: String?, memberCount: Int , onClick: () -> Unit) {
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
fun ChatMessageItem(message: Message, currentUserId: String) {
    val isMe = message.senderId == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            // Timestamp
            Row(
                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (message.timestamp != null && message.timestamp > 0) {
                        SimpleDateFormat(
                            "hh:mm a",
                            Locale.getDefault()
                        ).format(Date(message.timestamp))
                    } else {
                        ""
                    },
                    fontFamily = Nunito,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF777777)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Message bubble
            Box(
                modifier = Modifier
                    .background(
                        if (isMe) Color(0xFF607D8B) else Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
            ) {
                Text(
                    text = message.message,
                    color = if (isMe) Color.White else Color(0xFF555555),
                    fontSize = 14.sp,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SplitCard(split: Split, currentUserId: String , onClick: () -> Unit , viewModel: ChatViewModel, groupId: String) {
    val isMe = split.senderId == currentUserId
    val toPayAmount = split.splits[currentUserId] ?: 0.0
    val status = split.statusMap[currentUserId] ?: "pending"
    val textColor = if (isMe) Color.White else Color.Black
    val subTextColor = if (isMe) Color.White.copy(alpha = 0.8f) else Color.Gray
    val statusColor = if (status == "completed") Color(0xFF4CAF50) else Color(0xFFEB3223)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                //.padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isMe) Color(0xFF607D8B) else Color.White)
                .clickable { onClick() }
                .padding(16.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rs.${split.amount}",
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "To Pay: ${split.splits[currentUserId] ?: 0.0}",
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = subTextColor
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        //modifier = Modifier.size(12.dp),
                        tint = textColor
                    )
                }
                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (isMe) Color.White.copy(alpha = 0.2f) else Color.LightGray
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (split.taggedMembers.contains(currentUserId)) {
                        Text(
                            "You + ",
                            fontFamily = Nunito,
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${split.statusMap[currentUserId]}",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (isMe) Color.White else statusColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitPopUpDialog(
    split: Split,
    currentUserId: String ,
    groupId: String,
    onDismissClick: () -> Unit,
    viewModel: ChatViewModel
){
    var isChecked by remember { mutableStateOf(split.statusMap[currentUserId] == "completed") }
    AlertDialog(
        onDismissRequest = {onDismissClick()},
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp))
    {
        Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Total Amount: Rs.${split.amount.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontFamily = Nunito,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = split.description,
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFF607D8B),)
                    Text(
                        text = "  split between  ",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = Color(0xFF607D8B),
                        fontWeight = FontWeight.Medium
                    )
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFF607D8B),)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                    Text(
                        text = "Your share: Rs.${split.splits[currentUserId]?.toInt() ?: 0}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    isChecked = it
                                    if (it) {
                                        viewModel.markPaymentComplete(groupId = groupId, splitId = split.id,currentUserId)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF607D8B)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "I completed my payment",
                                fontFamily = Poppins,
                                fontSize = 10.sp,
                                color = Color(0xFF607D8B)
                            )
                        }

                }
            }
}