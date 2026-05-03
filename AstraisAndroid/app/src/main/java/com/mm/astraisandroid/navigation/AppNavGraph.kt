package com.mm.astraisandroid.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mm.astraisandroid.ui.core.MainViewModel
import com.mm.astraisandroid.ui.features.auth.LoginScreen
import com.mm.astraisandroid.ui.features.auth.OnboardingScreen
import com.mm.astraisandroid.ui.features.auth.RegisterScreen
import com.mm.astraisandroid.ui.features.home.HomeContainer
import com.mm.astraisandroid.ui.features.profile.PerfilTab
import com.mm.astraisandroid.ui.features.profile.UserViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: Route,
    mainViewModel: MainViewModel,
    userViewModel: UserViewModel,
    sessionManager: com.mm.astraisandroid.data.preferences.SessionManager,
    onDeepLinkConsumed: () -> Unit,
    onLogout: () -> Unit
) {
    val mainState by mainViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(mainState.isSessionActive) {
        val currentRoute = navController.currentDestination?.route
        val isAuthRoute = currentRoute == Route.Login::class.qualifiedName ||
                currentRoute == Route.Register::class.qualifiedName
        if (!mainState.isSessionActive && !isAuthRoute) {
            navController.navigate(Route.Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(mainState.pendingDeepLink) {
        if (mainState.pendingDeepLink == "navigate_to_groups") {
            navController.navigate(Route.GroupTab) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            mainViewModel.onDeepLinkConsumed()
        }
    }

    LaunchedEffect(mainState.shouldNavigateToOnboarding) {
        if (mainState.shouldNavigateToOnboarding) {
            navController.navigate(Route.Onboarding) {
                popUpTo(0) { inclusive = true }
            }
            mainViewModel.onOnboardingConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                if (!sessionManager.isGuest()) {
                    mainViewModel.checkOnboarding()
                }
            }
            LaunchedEffect(mainState.shouldNavigateToOnboarding) {
                if (mainState.shouldNavigateToOnboarding) {
                    mainViewModel.onOnboardingConsumed()
                    navController.navigate(Route.Onboarding) {
                        popUpTo(Route.Home) { inclusive = true }
                    }
                }
            }
            HomeContainer(
                userViewModel = userViewModel,
                sessionManager = sessionManager,
                onNavigateToProfile = {
                    navController.navigate(Route.Profile)
                },
                onLogout = onLogout
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
                isGuest = sessionManager.isGuest(),
                onBack = { navController.popBackStack() },
                onLogout = {
                    if (sessionManager.isGuest()) {
                        navController.navigate(Route.Register)
                    } else {
                        onLogout()
                    }
                }
            )
        }
    }
}
