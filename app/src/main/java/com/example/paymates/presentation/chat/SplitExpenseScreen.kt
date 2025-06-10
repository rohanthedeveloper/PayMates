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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.clickable {
                navController.popBackStack()
            })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Split Expense", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

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
                    }
                )
                Text("Select all")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = equalSplit,
                    onCheckedChange = { equalSplit = it }
                )
                Text("Equal split")
            }
        }

        LazyColumn {
            items(members) { user ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = selectedMembers.contains(user),
                        onCheckedChange = {
                            selectedMembers = if (it) selectedMembers + user else selectedMembers - user
                        }
                    )
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name)
                    }
                    if (!equalSplit && selectedMembers.contains(user)) {
                        OutlinedTextField(
                            value = manualSplits[user.uid] ?: "",
                            onValueChange = { value ->
                                manualSplits = manualSplits.toMutableMap().apply { put(user.uid, value) }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(80.dp)
                        )
                    } else if (equalSplit && selectedMembers.contains(user)) {
                        val perPerson = (amount.toDoubleOrNull() ?: 0.0) / (selectedMembers.size.takeIf { it > 0 } ?: 1)
                        Text(text = "₹%.2f".format(perPerson), modifier = Modifier.padding(end = 16.dp))
                    }
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
                .height(50.dp)
        ) {
            Text("Share split")
        }
    }
