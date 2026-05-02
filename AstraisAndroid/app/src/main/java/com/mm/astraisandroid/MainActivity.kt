package com.mm.astraisandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.navigation.AppNavGraph
import com.mm.astraisandroid.ui.components.HideSystemBarsEffect
import com.mm.astraisandroid.ui.core.MainViewModel
import com.mm.astraisandroid.ui.features.profile.UserViewModel
import com.mm.astraisandroid.ui.theme.AstraisandroidTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private val pendingDeepLinkUrl = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pendingDeepLinkUrl.value = intent?.dataString

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            val userState by userViewModel.state.collectAsStateWithLifecycle()

            val deepLink = pendingDeepLinkUrl.value
            if (deepLink != null) {
                mainViewModel.onDeepLinkReceived(deepLink)
                pendingDeepLinkUrl.value = null
            }

            AstraisandroidTheme(userTheme = userState.user?.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        startDestination = if (sessionManager.hasAnySession()) {
                            com.mm.astraisandroid.navigation.Route.Home
                        } else {
                            com.mm.astraisandroid.navigation.Route.Login
                        },
                        mainViewModel = mainViewModel,
                        userViewModel = userViewModel,
                        sessionManager = sessionManager,
                        onDeepLinkConsumed = { },
                        onLogout = { mainViewModel.onLogout() }
                    )
                    HideSystemBarsEffect()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingDeepLinkUrl.value = intent.dataString
    }
}
