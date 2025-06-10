package com.example.paymates.presentation.Navigation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.paymates.domain.GroupDetailUiState
import com.example.paymates.presentation.chat.ChatScreen
import com.example.paymates.presentation.GroupDetailScreen.GroupDetailScreen
import com.example.paymates.presentation.GroupDetailScreen.GroupDetailViewModel
import com.example.paymates.presentation.auth.AuthScreen
import com.example.paymates.presentation.chat.SplitExpenseScreen
import com.example.paymates.presentation.contacts.AddMemberScreen
import com.example.paymates.presentation.contacts.ContactScreen
import com.example.paymates.presentation.createGroup.CreateGroupScreen
import com.example.paymates.presentation.createGroup.GroupViewModel
import com.example.paymates.presentation.home.HomeScreen
import com.example.paymates.presentation.profile.ProfileScreen
import com.example.paymates.presentation.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue


@Composable
fun PayMatesNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController , startDestination = PayMatesScreens.SplashScreen.name) {
        composable(PayMatesScreens.SplashScreen.name) {
            SplashScreen(navController)
        }
        composable(PayMatesScreens.AuthScreen.name){
            AuthScreen(navController = navController)
        }
        composable(PayMatesScreens.HomeScreen.name){
            HomeScreen(navController = navController)
        }
        composable(PayMatesScreens.ProfileScreen.name){
            ProfileScreen(navController = navController)
        }
        composable(PayMatesScreens.ContactScreen.name) {
            ContactScreen(navController)
        }
        composable(PayMatesScreens.CreateGroupScreen.name) {
            val viewModel: GroupViewModel = koinViewModel()
            CreateGroupScreen(viewModel , navController)
        }
        composable("${PayMatesScreens.GroupDetailScreen.name}/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupDetailScreen(navController = navController , groupId = groupId)
        }
        composable("${PayMatesScreens.AddMemberScreen.name}/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            AddMemberScreen(navController = navController, groupId = groupId)
        }
        composable("${PayMatesScreens.ChatScreen.name}/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            ChatScreen(navController, groupId )
        }
        composable(
            route = "${PayMatesScreens.SplitExpenseScreen.name}/{groupId}/{senderId}/{senderName}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("senderId") { type = NavType.StringType },
                navArgument("senderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            val senderId = backStackEntry.arguments?.getString("senderId") ?: return@composable
            val senderName = backStackEntry.arguments?.getString("senderName") ?: return@composable

            // You may want to fetch the group members here
            val groupDetailViewModel: GroupDetailViewModel = koinViewModel()
            val uiState by groupDetailViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(groupId) {
                groupDetailViewModel.loadGroupDetails(groupId)
            }

            when (val state = uiState) {
                is GroupDetailUiState.GroupLoaded -> {
                    SplitExpenseScreen(
                        navController = navController,
                        groupId = groupId,
                        senderId = senderId,
                        senderName = senderName,
                        members = state.members
                    )
                }

                is GroupDetailUiState.Loading -> {
                    CircularProgressIndicator()
                }

                else -> {
                    Text("Error loading members")
                }
            }
        }

    }
}