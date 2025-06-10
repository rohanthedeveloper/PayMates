package com.example.paymates.presentation.contacts

import android.Manifest
import android.R
import android.content.Intent
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactScreen(navController: NavController) {
    val viewModel: ContactViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    // Trigger loading contacts when permission is granted
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
            permissionState.status.isGranted -> ShowContacts(state, viewModel::loadContacts)
            permissionState.status.shouldShowRationale -> {
                Text("This app needs access to your contacts to find friends.")
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Allow Access")
                }
            }
            else -> {
                Text("Sync contacts to find friends.")
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Sync Contacts")
                }
            }
        }
    }
}

@Composable
fun ShowContacts(state: ContactUiState, onRetry: () -> Unit) {
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

        else -> ContactsList(state.contacts)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsList(contacts: Map<String, List<Contact>>) {
    LazyColumn {
        contacts.forEach { (title, contactList) ->
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
            items(contactList) { contact ->
                ContactListItem(contact)
            }
        }
    }
}

@Composable
fun ContactListItem(contact: Contact) {
    val context = LocalContext.current
    val isRegistered = contact.isRegistered

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

        if (!isRegistered) {
            // ✅ Show "Invite" button for unregistered contacts
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Join me on Paymates! Download here: [App Link]")
                }
                context.startActivity(Intent.createChooser(intent, "Invite via"))
            }) {
                Icon(Icons.Filled.PersonAdd, contentDescription = "Invite", tint = Color.Gray)
            }
        }
    }
}
