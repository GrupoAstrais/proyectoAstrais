package com.mm.astraisandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mm.astraisandroid.api.BackendRepository
import com.mm.astraisandroid.api.LoginRequest
import com.mm.astraisandroid.api.LoginUIState
import com.mm.astraisandroid.api.LoginViewModel
import com.mm.astraisandroid.api.RegisterRequest
import com.mm.astraisandroid.ui.auth.screens.LoginScreen
import com.mm.astraisandroid.ui.auth.screens.RegisterScreen

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.Login.route
    ) {
        composable(ScreenRoutes.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(ScreenRoutes.Home.route) {
                        popUpTo(ScreenRoutes.Login.route) { inclusive = true }
                    }
                },

                onNavigateToRegister = {
                    navController.navigate(ScreenRoutes.Register.route)
                }

            )
        }

        composable(ScreenRoutes.Register.route){
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(ScreenRoutes.Login.route){
                        popUpTo(ScreenRoutes.Login.route) { inclusive = true }
                    }
                },

                onRegisterSuccess = {
                    navController.navigate(ScreenRoutes.Login.route)
                }

            )
        }

        composable(ScreenRoutes.Home.route) {
            HomeScreen()
        }
    }
}

@Composable
fun HomeScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        NavItem("Random pero index", Icons.Filled.AddCircle),
        NavItem("Random", Icons.Filled.Search),
        NavItem("Random", Icons.Filled.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    if (item.title == "Random pero index") {
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(50.dp)) },
                            label = { Text(item.title) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    } else {
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }


                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                0 -> Text("Inicio")
                1 -> Text("Cosas")
                2 -> Text("Cositas")
            }
        }
    }
}

data class NavItem(val title: String, val icon: ImageVector)

