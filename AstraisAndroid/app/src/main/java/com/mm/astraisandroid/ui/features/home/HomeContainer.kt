package com.mm.astraisandroid.ui.features.home


import com.mm.astraisandroid.R
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.res.stringResource
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
import com.mm.astraisandroid.ui.features.logros.LogrosScreen
import com.mm.astraisandroid.ui.features.profile.PerfilTab
import com.mm.astraisandroid.ui.features.store.InventarioTab
import com.mm.astraisandroid.ui.features.store.TiendaTab
import com.mm.astraisandroid.ui.features.tasks.CreateTareaDialog
import com.mm.astraisandroid.ui.features.tasks.TaskViewModel
import com.mm.astraisandroid.ui.features.tasks.TasksTab
import com.mm.astraisandroid.util.ConnectivityObserver

/**
 * Contenedor principal de la aplicación que alberga el Scaffold, la barra de navegación
 * inferior y el NavHost secundario con todas las pantallas de las tabs principales.
 *
 * Gestiona la observación de conectividad, sincronización al recuperar conexión,
 * visualización de banners de estado (offline/invitado) y el diálogo global de creación de tareas.
 *
 * @param userViewModel ViewModel para obtener y refrescar datos del usuario.
 * @param sessionManager Gestor de sesión para verificar estado de autenticación.
 * @param onNavigateToProfile Callback para navegar fuera del contenedor al perfil.
 * @param onLogout Callback ejecutado al cerrar sesión.
 */
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

    val showStatusBanner = isGuest || !deviceHasInternet || isOffline

    AuthBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.statusBarsPadding(),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                                snackbarViewModel.showMessage(context.getString(R.string.guest_register_to_access))
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
                        },
                        onNavigateToLogros = {
                            navController.navigate(Route.Logros) {
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

                composable<Route.Logros> {
                    LogrosScreen(onBack = { navController.popBackStack() })
                }
            }
        }

        if (showStatusBanner) {
            GlassStatusBanner(
                text = when {
                    isGuest -> stringResource(R.string.banner_guest_mode)
                    !deviceHasInternet -> stringResource(R.string.banner_offline)
                    else -> stringResource(R.string.banner_offline_local)
                },
                color = if (isGuest) Color(0xFFC172FF) else Color(0xFFEF476F),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            )
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
                    snackbarViewModel.showMessage(context.getString(R.string.error_no_personal_group))
                }

                taskViewModel.closeCreateDialog()
            }
        )
    }
}

/**
 * Banner de estado con estilo glassmorphism que muestra información contextual
 * sobre el modo actual de la aplicación (invitado, sin conexión, datos locales).
 *
 * @param text Texto descriptivo del estado actual.
 * @param color Color del texto y borde del banner.
 * @param modifier Modificador de composición para personalizar posición y estilo.
 */
@Composable
private fun GlassStatusBanner(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.25f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
