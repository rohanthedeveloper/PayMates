package com.example.paymates.presentation.chat

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.paymates.data.User
import com.example.paymates.domain.GroupDetailUiState
import com.example.paymates.presentation.GroupDetailScreen.GroupDetailViewModel
import com.example.paymates.ui.theme.Poppins
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign


@Composable
fun SplitExpenseScreen(
    groupId: String,
    senderId: String,
    senderName: String,
    members: List<User>,
    navController: NavController,
    viewModel: ChatViewModel = koinViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var equalSplit by remember { mutableStateOf(true) }
    var selectedMembers by remember { mutableStateOf<List<User>>(emptyList()) }
    var manualSplits by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val context = LocalContext.current

    Scaffold(
        topBar = { SplitExpenseTopAppBar(navController = navController) },
        modifier = Modifier.systemBarsPadding()
    ) { it ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(it)
                .background(Color(0xFFECEFF1))
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder = { Text("Amount") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedMembers.size == members.size,
                        onCheckedChange = {
                            selectedMembers = if (it) members else emptyList()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF607D8B),
                            uncheckedColor = Color(0xFF607D8B)
                        )
                    )
                    Text(
                        "Select all",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF607D8B)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = equalSplit,
                        onCheckedChange = { equalSplit = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF607D8B),
                            uncheckedColor = Color(0xFF607D8B)
                        )
                    )
                    Text(
                        "Equal split",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF607D8B)
                    )
                }
            }

            LazyColumn {
                items(members) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = selectedMembers.contains(user),
                            onCheckedChange = {
                                selectedMembers =
                                    if (it) selectedMembers + user else selectedMembers - user
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF607D8B),
                                uncheckedColor = Color(0xFF607D8B)
                            )
                        )
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                user.name,
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF607D8B)
                            )
                        }
                        if (!equalSplit && selectedMembers.contains(user)) {
                            BasicTextField(
                                value = manualSplits[user.uid] ?: "",
                                onValueChange = { value ->
                                    manualSplits =
                                        manualSplits.toMutableMap().apply { put(user.uid, value) }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF5D7684),
                                    fontSize = 14.sp,
                                    fontFamily = Poppins
                                ),
                                modifier = Modifier
                                    .width(80.dp)
                                    .padding(bottom = 4.dp),
                                decorationBox = { innerTextField ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        innerTextField()
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(Color(0xFF5D7684)) // underline color
                                        )
                                    }
                                }
                            )
                        } else if (equalSplit && selectedMembers.contains(user)) {
                            val perPerson = (amount.toDoubleOrNull()
                                ?: 0.0) / (selectedMembers.size.takeIf { it > 0 } ?: 1)
                            Text(
                                text = ".2f".format(perPerson),
                                modifier = Modifier.padding(end = 16.dp),
                                color = Color(0xFF5D7684),
                                fontSize = 14.sp,
                                fontFamily = Poppins
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val totalAmount = amount.toDoubleOrNull()
                    if (totalAmount == null) {
                        Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val manualSplitMap = manualSplits.mapValues { it.value.toDoubleOrNull() ?: 0.0 }

                    viewModel.submitSplit(
                        groupId = groupId,
                        senderId = senderId,
                        senderName = senderName,
                        amount = totalAmount,
                        description = description,
                        selectedMembers = selectedMembers,
                        splitType = if (equalSplit) "equal" else "manual",
                        manualSplits = manualSplitMap,
                        onSuccess = {
                            Toast.makeText(context, "Split shared", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onFailure = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                enabled = amount.isNotBlank() && description.isNotBlank() && selectedMembers.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A2540))
            ) {
                Text(
                    "Share split",
                    fontFamily = Poppins,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SplitExpenseTopAppBar(navController: NavController) {
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
            text = "Split Expense",
            fontFamily = Poppins,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
