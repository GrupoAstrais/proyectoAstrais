package com.mm.astraisandroid

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mm.astraisandroid.ui.auth.components.AuthBackground
import com.mm.astraisandroid.ui.auth.screens.LoginScreen
import com.mm.astraisandroid.ui.auth.screens.RegisterScreen
import com.mm.astraisandroid.ui.tabs.GrupoTab
import com.mm.astraisandroid.ui.tabs.HomeTab
import com.mm.astraisandroid.ui.tabs.PerfilTab
import com.mm.astraisandroid.ui.tabs.TasksTab
import com.mm.astraisandroid.ui.tabs.TiendaTab
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.ui.tabs.ClickerGameScreen
import com.mm.astraisandroid.ui.theme.AstraisandroidTheme
import com.mm.astraisandroid.ui.viewmodels.TaskViewModel
import com.mm.astraisandroid.ui.viewmodels.UserViewModel
import com.mm.astraisandroid.util.ConnectivityObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userViewModel: UserViewModel = hiltViewModel()
            val userData by userViewModel.userData.collectAsStateWithLifecycle()

            AstraisandroidTheme(themeJson = userData?.themeColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(initialHasSession = SessionManager.hasSession())
                    OcultarBotonesSistema()
                }
            }
        }
    }
}

@Composable
fun AppNavigation(initialHasSession: Boolean) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = if (initialHasSession) ScreenRoutes.Home.route else ScreenRoutes.Login.route
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
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(ScreenRoutes.Profile.route)
                }
            )
        }



        composable(ScreenRoutes.Profile.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            val userData by userViewModel.userData.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                if (userData == null) {
                    userViewModel.fetchUser()
                }
            }

            PerfilTab(
                user = userData,
                onBack = { navController.popBackStack() },
                onLogout = {
                    coroutineScope.launch {
                        SessionManager.clear()
                        navController.navigate(ScreenRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(onNavigateToProfile: () -> Unit) {
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val deviceHasInternet by connectivityObserver.status.collectAsState(initial = true)

    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val taskViewModel: TaskViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        userViewModel.fetchUser()
    }

    LaunchedEffect(deviceHasInternet) {
        if (deviceHasInternet) {
            userViewModel.fetchUser()

            val gid = SessionManager.getPersonalGid()
            if (gid != null) {
                taskViewModel.syncOfflineActions(gid)
            }
        }
    }

    val userData by userViewModel.userData.collectAsStateWithLifecycle()
    val isOffline by userViewModel.isOffline.collectAsStateWithLifecycle()

    val isEffectivelyOffline = !deviceHasInternet || isOffline
    AuthBackground {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            topBar = {
                if (isEffectivelyOffline) {
                    Box(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFEF476F)).padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Modo sin conexión - Los cambios se sincronizarán después",
                            color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            },
            containerColor = Color.Transparent,
            bottomBar = {
                NavBottomBar(
                    selected = when (currentRoute) {
                        "home_tab"      -> 0
                        "tasks_tab"     -> 1
                        "add_tab"       -> 2
                        "group_tab"     -> 3
                        "store_tab"     -> 4
                        "minigames_tab" -> 5
                        "inventory_tab" -> 0
                        else            -> 0
                    },
                    onSelect = { index ->
                        if (isEffectivelyOffline && (index == 3 || index == 4)) {
                            Toast.makeText(context, "No disponible sin conexión", Toast.LENGTH_SHORT).show()
                            return@NavBottomBar
                        }

                        if (index == 2) {
                            taskViewModel.openCreateDialog()
                            return@NavBottomBar
                        }

                        val route = when (index) {
                            0 -> "home_tab"
                            1 -> "tasks_tab"
                            3 -> "group_tab"
                            4 -> "store_tab"
                            5 -> "minigames_tab"
                            else -> "home_tab"
                        }

                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "home_tab",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("home_tab") {
                    HomeTab(
                        userData = userData,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToTasks = {
                            navController.navigate("tasks_tab") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToInventory = {
                            navController.navigate("inventory_tab") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToStore = {
                            navController.navigate("store_tab") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToGroups = {
                            navController.navigate("group_tab") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable("tasks_tab") {
                    TasksTab(
                        viewModel = taskViewModel,
                        onTaskCompleted = { userViewModel.fetchUser() }
                    )
                }
                composable("group_tab") { GrupoTab() }

                composable("store_tab") {
                    TiendaTab(ludiones = userData?.ludiones ?: 0, onCosmeticChanged = { userViewModel.fetchUser() })
                }

                composable("inventory_tab") {
                    com.mm.astraisandroid.ui.tabs.InventarioTab(onCosmeticChanged = { userViewModel.fetchUser() })
                }

                composable("minigames_tab") {
                    ClickerGameScreen(
                        url = "http://192.168.1.129:5684/static/minigames/clicker.html",
                        onScoreSubmit = { puntos ->
                            Toast.makeText(context, "¡Has ganado $puntos Ludiones!", Toast.LENGTH_LONG).show()

                            navController.navigate("home_tab") {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }

    if (taskViewModel.showCreateDialog) {
        CreateTareaDialog(
            onDismiss = { taskViewModel.closeCreateDialog() },
            onCreate  = { titulo, desc, tipo, prioridad ->
                val userGid = SessionManager.getPersonalGid()

                if (userGid != null) {
                    taskViewModel.crearTarea(
                        gid         = userGid,
                        titulo      = titulo,
                        descripcion = desc,
                        tipo        = tipo,
                        prioridad   = prioridad
                    )
                } else {
                    Toast.makeText(context, "Error: Usuario sin grupo personal.", Toast.LENGTH_LONG).show()
                }

                taskViewModel.closeCreateDialog()
            }
        )
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
fun NavBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Tasks", Icons.Filled.CheckCircle),
        NavItem("Add", Icons.Filled.AddCircle),
        NavItem("Groups", Icons.Filled.Person),
        NavItem("Store", Icons.Filled.ShoppingCart),
        NavItem("Minigames", Icons.Filled.Games)
    )

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
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isSelected && !isCenter,
                    transitionSpec = {
                        scaleIn(initialScale = 0.7f, animationSpec = tween(300)) +
                                fadeIn(tween(300)) togetherWith
                                scaleOut(targetScale = 0.7f, animationSpec = tween(300)) +
                                fadeOut(tween(300))
                    },
                    label = "tab_anim_${item.title}"
                ) { showText ->
                    if (showText) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = when {
                                isCenter -> Color.Black
                                else -> Color.White.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(if (isCenter) 28.dp else 22.dp)
                        )
                    }
                }
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