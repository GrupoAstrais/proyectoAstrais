package com.mm.astraisandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
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
import com.mm.astraisandroid.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity principal de la aplicación Astrais.
 *
 * Configura la UI con Compose, gestiona deep links e inicializa el grafo de navegación.
 * Determina el destino inicial según si el usuario tiene una sesión activa.
 * Soporta cambios de idioma vía [LocaleHelper] y display edge-to-edge.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Gestor de sesión para verificación de estado de autenticación. */
    @Inject
    lateinit var sessionManager: SessionManager

    /** Contiene una URL de deep link pendiente del intent de lanzamiento, limpiada tras procesarse. */
    private val pendingDeepLinkUrl = mutableStateOf<String?>(null)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pendingDeepLinkUrl.value = intent?.dataString

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            val userState by userViewModel.state.collectAsStateWithLifecycle()

            var navRestartKey by remember { mutableIntStateOf(0) }

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
                    key(navRestartKey) {
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
                            onLogout = {
                                mainViewModel.onLogout()
                                navRestartKey++
                            }
                        )
                    }
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
