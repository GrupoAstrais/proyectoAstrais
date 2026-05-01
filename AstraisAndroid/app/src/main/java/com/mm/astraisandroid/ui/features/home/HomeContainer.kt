package com.mm.astraisandroid.ui.features.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.toRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mm.astraisandroid.data.models.TaskPriority
import com.mm.astraisandroid.data.models.TaskType
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.navigation.Route
import com.mm.astraisandroid.ui.components.AstraisBottomBar
import com.mm.astraisandroid.ui.components.GlobalSnackbarViewModel
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import com.mm.astraisandroid.ui.features.groups.GrupoTab
import com.mm.astraisandroid.ui.features.groups.GroupDetailScreen
import com.mm.astraisandroid.ui.features.groups.GroupSettingsScreen
import com.mm.astraisandroid.ui.features.profile.PerfilTab
import com.mm.astraisandroid.ui.features.store.InventarioTab
import com.mm.astraisandroid.ui.features.store.TiendaTab
import com.mm.astraisandroid.ui.features.tasks.CreateTareaDialog
import com.mm.astraisandroid.ui.features.tasks.TaskViewModel
import com.mm.astraisandroid.ui.features.tasks.TasksTab
import com.mm.astraisandroid.util.ConnectivityObserver

@Composable
fun HomeContainer(
    userViewModel: com.mm.astraisandroid.ui.features.profile.UserViewModel,
    sessionManager: SessionManager,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val deviceHasInternet by connectivityObserver.status.collectAsState(initial = true)

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarViewModel: GlobalSnackbarViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        snackbarViewModel.snackbarEvents.collect { event ->
            snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.actionLabel,
                duration = SnackbarDuration.Short
            )
        }
    }

    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val taskViewModel: TaskViewModel = hiltViewModel()

    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val userState by userViewModel.state.collectAsStateWithLifecycle()
    val userData = userState.user
    val isOffline = userState.isOffline

    val isGuest = sessionManager.isGuest()

    LaunchedEffect(Unit) {
        if (!isGuest) {
            userViewModel.fetchUser()
        }
    }

    LaunchedEffect(deviceHasInternet, isGuest) {
        if (deviceHasInternet && !isGuest) {
            val gid = sessionManager.getPersonalGid()
            if (gid != null) {
                taskViewModel.syncOfflineActionsAwait(gid)
            }
            userViewModel.fetchUser()
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRouteName = currentBackStackEntry?.destination?.route

    val isInGroupSection = currentRouteName?.contains(Route.GroupTab::class.qualifiedName!!) == true ||
            currentRouteName?.contains(Route.GroupDetail::class.qualifiedName!!) == true ||
            currentRouteName?.contains(Route.GroupSettings::class.qualifiedName!!) == true

    val selectedIndex = when {
        currentRouteName?.contains(Route.Home::class.qualifiedName!!) == true -> 0
        currentRouteName?.contains(Route.TasksTab::class.qualifiedName!!) == true -> 1
        isInGroupSection -> 3
        currentRouteName?.contains(Route.StoreTab::class.qualifiedName!!) == true -> 4
        else -> 0
    }

    val showBottomBar = currentRouteName?.contains(Route.GroupDetail::class.qualifiedName!!) != true &&
            currentRouteName?.contains(Route.GroupSettings::class.qualifiedName!!) != true

    val isEffectivelyOffline = !deviceHasInternet || isOffline || isGuest

    AuthBackground {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                val bannerText = when {
                    isGuest -> "MODO INVITADO — Registrate para guardar tus tareas"
                    !deviceHasInternet -> "SIN CONEXIÓN — Se sincronizará al volver"
                    isOffline -> "MODO OFFLINE — Acceso local activo"
                    else -> null
                }

                if (bannerText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isGuest) Color(0xFFC172FF) else Color(0xFFEF476F))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bannerText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    AstraisBottomBar(
                        selected = selectedIndex,
                        isGuest = isGuest,
                        onSelect = { index ->
                            if (index == 2) {
                                taskViewModel.openCreateDialog()
                                return@AstraisBottomBar
                            }

                            if (isGuest && (index == 3 || index == 4)) {
                                Toast.makeText(context, "Regístrate para acceder a esta función", Toast.LENGTH_SHORT).show()
                                return@AstraisBottomBar
                            }

                            val route = when (index) {
                                0 -> Route.Home
                                1 -> Route.TasksTab
                                3 -> Route.GroupTab
                                4 -> Route.StoreTab
                                else -> Route.Home
                            }

                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Route.Home,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable<Route.Home> {
                    HomeTab(
                        user = userData,
                        isGuest = isGuest,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToTasks = {
                            navController.navigate(Route.TasksTab) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToInventory = {
                            navController.navigate(Route.Inventory) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToStore = {
                            navController.navigate(Route.StoreTab) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToGroups = {
                            navController.navigate(Route.GroupTab) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable<Route.TasksTab> {
                    TasksTab(
                        viewModel = taskViewModel,
                        onTaskCompleted = { userViewModel.fetchUser() }
                    )
                }
                composable<Route.GroupTab> {
                    GrupoTab(
                        onOpenGroup = { g ->
                            navController.navigate(
                                Route.GroupDetail(
                                    gid = g.id,
                                    role = g.role,
                                    name = g.name,
                                    description = g.subtitle
                                )
                            )
                        }
                    )
                }

                composable<Route.GroupDetail> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.GroupDetail>()
                    GroupDetailScreen(
                        gid = args.gid,
                        groupName = args.name,
                        groupDescription = args.description,
                        groupRole = args.role,
                        onBack = { navController.popBackStack() },
                        onUserStateChanged = { userViewModel.fetchUser() },
                        onOpenSettings = {
                            navController.navigate(
                                Route.GroupSettings(
                                    gid = args.gid,
                                    role = args.role,
                                    name = args.name,
                                    description = args.description
                                )
                            )
                        }
                    )
                }

                composable<Route.GroupSettings> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.GroupSettings>()
                    GroupSettingsScreen(
                        gid = args.gid,
                        groupName = args.name,
                        groupDescription = args.description,
                        groupRole = args.role,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<Route.StoreTab> {
                    TiendaTab(ludiones = userData?.ludiones ?: 0, onCosmeticChanged = { userViewModel.fetchUser() })
                }

                composable<Route.Inventory> {
                    InventarioTab(onCosmeticChanged = { userViewModel.fetchUser() })
                }
            }
        }
    }

    if (taskState.showCreateDialog) {
        CreateTareaDialog(
            parentId = taskState.parentIdForNewTask,
            onDismiss = { taskViewModel.closeCreateDialog() },
            onCreate = { titulo, desc, tipoStr, prioridadInt, frecuencia, fechaLimite ->
                val userGid = sessionManager.getPersonalGid() ?: if (sessionManager.isGuest()) -1 else null

                if (userGid != null) {
                    val tipoEnum = runCatching { TaskType.valueOf(tipoStr) }.getOrDefault(TaskType.UNICO)
                    val prioridadEnum = when (prioridadInt) {
                        0 -> TaskPriority.LOW
                        1 -> TaskPriority.MEDIUM
                        2 -> TaskPriority.HIGH
                        else -> TaskPriority.LOW
                    }

                    taskViewModel.crearTarea(
                        gid = userGid,
                        titulo = titulo,
                        descripcion = desc,
                        tipo = tipoEnum,
                        prioridad = prioridadEnum,
                        fechaLimite = fechaLimite,
                        frecuencia = frecuencia
                    )
                } else {
                    Toast.makeText(context, "Error: Usuario sin grupo personal.", Toast.LENGTH_LONG).show()
                }

                taskViewModel.closeCreateDialog()
            }
        )
    }
}
