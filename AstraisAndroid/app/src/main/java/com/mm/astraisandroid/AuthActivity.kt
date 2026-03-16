package com.mm.astraisandroid

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mm.astraisandroid.api.BackendRepository
import com.mm.astraisandroid.api.LoginRequest
import com.mm.astraisandroid.api.LoginUIState
import com.mm.astraisandroid.api.LoginViewModel
import com.mm.astraisandroid.api.RegisterRequest
import com.mm.astraisandroid.ui.auth.components.AuthBackground
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

                    OcultarBotonesSistema()
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
/*
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
}*/

@Composable
fun HomeScreen() {
    AuthBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { NavBottomBar() }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 24.dp).consumeWindowInsets(PaddingValues(16.dp)),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeHeader(username = "Astrais")
                //BentoGrid()
                //NotificationsBar(count = 8)
            }
        }
    }
}

@Composable
fun HomeHeader(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hi, $username",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "P",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun NavBottomBar() {
    val items = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Tasks", Icons.Filled.CheckCircle),
        NavItem("Add", Icons.Filled.AddCircle),
        NavItem("Store", Icons.Filled.ShoppingCart),
        NavItem("Profile", Icons.Filled.Person)
    )
    var selected by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x16FFFFFF))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selected == index
            val isCenter = index == 2

            Box(
                modifier = Modifier
                    .size(if (isCenter) 54.dp else 44.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCenter -> Color.White
                            isSelected -> Color.White.copy(alpha = 0.15f)
                            else -> Color.Transparent
                        }
                    )
                    .clickable { selected = index },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = when {
                        isCenter -> Color.Black
                        isSelected -> Color.White
                        else -> Color.White.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(if (isCenter) 28.dp else 22.dp)
                )
            }
        }
    }
}


data class NavItem(val title: String, val icon: ImageVector)

@Composable
fun OcultarBotonesSistema() {
    val view = LocalView.current

    SideEffect {
        val window = (view.context as android.app.Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}