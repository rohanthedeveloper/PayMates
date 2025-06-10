package com.example.paymates.presentation.contacts

import android.Manifest
import android.R
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.paymates.data.Contact
import com.example.paymates.domain.ContactUiState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.paymates.data.User
import com.example.paymates.presentation.GroupDetailScreen.GroupDetailViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddMemberScreen(navController: NavController, groupId: String) {
    val viewModel: ContactViewModel = koinViewModel()
    val groupViewModel: GroupDetailViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    // Load contacts when permission is granted
    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            viewModel.loadContacts()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            permissionState.status.isGranted -> ShowAddMemberContacts(state, groupId, groupViewModel, navController, viewModel::loadContacts)
            permissionState.status.shouldShowRationale -> {
                Text("This app needs access to your contacts to add members.")
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Allow Access")
                }
            }
            else -> {
                Text("Sync contacts to add members.")
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Sync Contacts")
                }
            }
        }
    }
}

@Composable
fun ShowAddMemberContacts(
    state: ContactUiState,
    groupId: String,
    groupViewModel: GroupDetailViewModel,
    navController: NavController,
    onRetry: () -> Unit
) {
    when {
        state.loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("Loading contacts...")
            }
        }

        state.error != null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error: ${state.error}")
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }

        state.contacts.isEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No contacts found.")
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }

        else -> AddMemberContactsList(state.contacts, groupId, groupViewModel, navController)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddMemberContactsList(
    contacts: Map<String, List<Contact>>,
    groupId: String,
    groupViewModel: GroupDetailViewModel,
    navController: NavController
) {
    LazyColumn {
        contacts.forEach { (title, contactList) ->
            // ✅ Filter out unregistered contacts
            val registeredContacts = contactList.filter { it.isRegistered }

            if (registeredContacts.isNotEmpty()) { // ✅ Show section only if it has registered users
                stickyHeader {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(12.dp)
                    )
                }
                items(registeredContacts) { contact ->
                    AddMemberContactItem(contact, groupId, groupViewModel, navController)
                }
            }
        }
    }
}

@Composable
fun AddMemberContactItem(
    contact: Contact,
    groupId: String,
    groupViewModel: GroupDetailViewModel,
    navController: NavController
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(end = 10.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = rememberAsyncImagePainter(contact.photoUri ?: R.drawable.divider_horizontal_dark),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(10.dp)
                .size(60.dp)
                .clip(CircleShape)
        )
        Column(modifier = Modifier.weight(1f, true)) {
            Text(
                text = contact.name.ifEmpty { "Unknown Contact" },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = contact.phoneNumber.ifEmpty { "N/A" },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // ✅ "Add Member" button
        IconButton(onClick = {
            groupViewModel.addMemberToGroup(groupId, contact.uid) { // ✅ Pass UID, not phone number
                Toast.makeText(context, "Member Added", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }) {
            Icon(Icons.Filled.PersonAdd, contentDescription = "Add", tint = Color.Green)
        }

    }
}
