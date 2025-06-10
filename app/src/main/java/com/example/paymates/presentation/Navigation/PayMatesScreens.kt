package com.example.paymates.presentation.Navigation

enum class PayMatesScreens {
    SplashScreen,
    AuthScreen,
    HomeScreen,
    ContactScreen,
    CreateGroupScreen,
    GroupDetailScreen,
    AddMemberScreen,
    ChatScreen,
    SplitExpenseScreen,
    ProfileScreen;

    companion object{
        fun fromRoute(route: String?): PayMatesScreens = when(route?.substringBefore("/")){
            SplashScreen.name -> SplashScreen
            AuthScreen.name -> AuthScreen
            HomeScreen.name -> HomeScreen
            ProfileScreen.name -> ProfileScreen
            ContactScreen.name -> ContactScreen
            CreateGroupScreen.name -> CreateGroupScreen
            GroupDetailScreen.name -> GroupDetailScreen
            AddMemberScreen.name -> AddMemberScreen
            ChatScreen.name -> ChatScreen
            SplitExpenseScreen.name -> SplitExpenseScreen
            null -> HomeScreen
            else -> throw IllegalArgumentException("route $route not found")
        }
    }
}