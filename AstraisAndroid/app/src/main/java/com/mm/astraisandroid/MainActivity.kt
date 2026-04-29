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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import com.mm.astraisandroid.ui.features.auth.LoginScreen
import com.mm.astraisandroid.ui.features.auth.RegisterScreen
import com.mm.astraisandroid.ui.features.auth.OnboardingScreen
import com.mm.astraisandroid.ui.features.groups.GrupoTab
import com.mm.astraisandroid.ui.features.home.HomeTab
import com.mm.astraisandroid.ui.features.profile.PerfilTab
import com.mm.astraisandroid.ui.features.tasks.TasksTab
import com.mm.astraisandroid.ui.features.store.TiendaTab
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.mm.astraisandroid.data.models.TaskPriority
import com.mm.astraisandroid.data.models.TaskType
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.repository.AuthRepository
import com.mm.astraisandroid.navigation.Route
import com.mm.astraisandroid.ui.features.tasks.CreateTareaDialog
import com.mm.astraisandroid.ui.features.store.InventarioTab
import com.mm.astraisandroid.ui.components.GlobalSnackbarViewModel
import com.mm.astraisandroid.ui.theme.AstraisandroidTheme
import com.mm.astraisandroid.ui.features.tasks.TaskViewModel
import com.mm.astraisandroid.ui.features.profile.UserViewModel
import com.mm.astraisandroid.util.ConnectivityObserver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.navigation.toRoute
import com.mm.astraisandroid.ui.features.groups.GroupDetailScreen
import com.mm.astraisandroid.ui.features.groups.GroupSettingsScreen
import com.mm.astraisandroid.data.repository.GroupRepository
import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var groupRepository: GroupRepository

    private val pendingDeepLinkUrl = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pendingDeepLinkUrl.value = intent?.dataString

        setContent {
            val userViewModel: UserViewModel = hiltViewModel()
            val userState by userViewModel.state.collectAsStateWithLifecycle()
            val userData = userState.user

            AstraisandroidTheme(userTheme = userData?.theme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val coroutineScope = rememberCoroutineScope()
                    AppNavigation(
                            initialHasSession = SessionManager.hasAnySession(),
                            userViewModel = userViewModel,
                            authRepository = authRepository,
                            groupRepository = groupRepository,
                            deepLinkUrl = pendingDeepLinkUrl.value,
                            onDeepLinkConsumed = { pendingDeepLinkUrl.value = null },
                            onLogout = {
                                coroutineScope.launch {
                                    authRepository.logout()
                                }
                            }
                        )
                    OcultarBotonesSistema()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingDeepLinkUrl.value = intent.dataString
    }
}

@Composable
fun AppNavigation(
    initialHasSession: Boolean,
    userViewModel: UserViewModel,
    authRepository: AuthRepository,
    groupRepository: GroupRepository,
    deepLinkUrl: String?,
    onDeepLinkConsumed: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val isSessionActive by SessionManager.isSessionActive.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val tryHandleJoinLink: suspend (String) -> Boolean = { url ->
        val uri = android.net.Uri.parse(url)
        val code = uri.getQueryParameter("code")
        runCatching {
            if (!code.isNullOrBlank()) {
                groupRepository.joinByCode(code)
            } else {
                groupRepository.joinByUrl(url)
            }
            true
        }.getOrDefault(false)
    }

    LaunchedEffect(isSessionActive) {
        if (!isSessionActive && navController.currentDestination?.route != Route.Login::class.qualifiedName && navController.currentDestination?.route != Route.Register::class.qualifiedName) {
            navController.navigate(Route.Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(deepLinkUrl, isSessionActive) {
        val url = deepLinkUrl ?: return@LaunchedEffect

        val isJoinLink = runCatching { android.net.Uri.parse(url) }.getOrNull()?.let { uri ->
            val isCustomScheme = uri.scheme == "astrais" && uri.host == "groups" && uri.path?.startsWith("/join") == true
            val isHttpsAppLink = uri.scheme == "https" && uri.host == "astrais.app" && uri.path?.startsWith("/groups/join") == true
            isCustomScheme || isHttpsAppLink
        } ?: false

        if (!isJoinLink) return@LaunchedEffect

        if (!isSessionActive || SessionManager.isGuest()) {
            SessionManager.savePendingDeepLink(url)
            Toast.makeText(context, "Inicia sesión para usar la invitación", Toast.LENGTH_SHORT).show()
            onDeepLinkConsumed()
            return@LaunchedEffect
        }

        val ok = tryHandleJoinLink(url)

        if (ok) {
            Toast.makeText(context, "Te has unido al grupo", Toast.LENGTH_SHORT).show()
            navController.navigate(Route.GroupTab) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            Toast.makeText(context, "No se pudo usar el enlace de invitación", Toast.LENGTH_LONG).show()
        }

        onDeepLinkConsumed()
    }

    LaunchedEffect(isSessionActive) {
        if (!isSessionActive || SessionManager.isGuest()) return@LaunchedEffect
        val pendingUrl = SessionManager.consumePendingDeepLink() ?: return@LaunchedEffect
        val ok = tryHandleJoinLink(pendingUrl)
        if (ok) {
            Toast.makeText(context, "Invitación aplicada tras iniciar sesión", Toast.LENGTH_SHORT).show()
            navController.navigate(Route.GroupTab) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            Toast.makeText(context, "No se pudo usar la invitación guardada", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(isSessionActive) {
        if (!isSessionActive || SessionManager.isGuest()) return@LaunchedEffect

        val shouldGoToOnboarding = runCatching { authRepository.needsOnboarding() }.getOrDefault(false)
        val onboardingRoute = Route.Onboarding::class.qualifiedName
        val currentRoute = navController.currentDestination?.route

        if (shouldGoToOnboarding && currentRoute != onboardingRoute) {
            navController.navigate(Route.Onboarding) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (initialHasSession) Route.Home else Route.Login,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable<Route.Login> {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Route.Register) },
                onNavigateToOnboarding = {
                    navController.navigate(Route.Onboarding) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Register> {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Route.Login) },
                onRegisterSuccess = {
                    navController.navigate(Route.Onboarding) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Route.Onboarding) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Onboarding> {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Onboarding) { inclusive = true }
                    }
                },
                userViewModel = userViewModel
            )
        }

        composable<Route.Home> {
            LaunchedEffect(Unit) {
                if (!SessionManager.isGuest()) {
                    val shouldGoToOnboarding = runCatching { authRepository.needsOnboarding() }.getOrDefault(false)
                    if (shouldGoToOnboarding) {
                        navController.navigate(Route.Onboarding) {
                            popUpTo(Route.Home) { inclusive = true }
                        }
                    }
                }
            }
            HomeScreen(
                userViewModel = userViewModel,
                onNavigateToProfile = {
                    navController.navigate(Route.Profile)
                }
            )
        }

        composable<Route.Profile> {
            val profileUserViewModel: UserViewModel = hiltViewModel()
            val userState by profileUserViewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                if (userState.user == null) {
                    profileUserViewModel.fetchUser()
                }
            }

            PerfilTab(
                user = userState.user,
                onBack = { navController.popBackStack() },
                onLogout = {
                    if (SessionManager.isGuest()) {
                        navController.navigate(Route.Register)
                    } else {
                        onLogout()
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    onNavigateToProfile: () -> Unit,
    snackbarViewModel: GlobalSnackbarViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val deviceHasInternet by connectivityObserver.status.collectAsState(initial = true)

    val snackbarHostState = remember { SnackbarHostState() }

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

    val isGuest = SessionManager.isGuest()

    LaunchedEffect(Unit) {
        if (!isGuest) {
            userViewModel.fetchUser()
        }
    }

    LaunchedEffect(deviceHasInternet, isGuest) {
        if (deviceHasInternet && !isGuest) {
            val gid = SessionManager.getPersonalGid()
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
                    NavBottomBar(
                        selected = selectedIndex,
                        isGuest = isGuest,
                        onSelect = { index ->
                            if (index == 2) {
                                taskViewModel.openCreateDialog()
                                return@NavBottomBar
                            }

                            if (isGuest && (index == 3 || index == 4)) {
                                Toast.makeText(context, "Regístrate para acceder a esta función", Toast.LENGTH_SHORT).show()
                                return@NavBottomBar
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
                val userGid = SessionManager.getPersonalGid() ?: if (SessionManager.isGuest()) -1 else null

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
fun NavBottomBar(selected: Int, isGuest: Boolean = false, onSelect: (Int) -> Unit) {
    val items = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Tasks", Icons.Filled.CheckCircle),
        NavItem("Add", Icons.Filled.AddCircle),
        NavItem("Groups", Icons.Filled.Person),
        NavItem("Store", Icons.Filled.ShoppingCart),
        //NavItem("Minigames", Icons.Filled.Games)
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
            val isDisabled = isGuest && (index == 3 || index == 4)

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
                                isDisabled -> Color.White.copy(alpha = 0.2f)
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