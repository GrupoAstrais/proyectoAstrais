package com.mm.astraisandroid.ui.features.auth

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mm.astraisandroid.BuildConfig
import com.mm.astraisandroid.R
import com.mm.astraisandroid.ui.components.GlobalSnackbarViewModel
import com.mm.astraisandroid.data.api.LoginRequest
import com.mm.astraisandroid.ui.theme.Background
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.ui.theme.Gray700
import com.mm.astraisandroid.ui.theme.Primary
import com.mm.astraisandroid.ui.theme.Secondary
import com.mm.astraisandroid.ui.theme.Surface
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión con soporte para email/contraseña, Google Sign-In y modo invitado.
 *
 * Muestra un formulario de autenticación con validación de campos, indicador de carga
 * y gestión de errores. Integra CredentialManager para autenticación con Google.
 *
 * @param onNavigateToHome Callback ejecutado tras login exitoso sin necesidad de onboarding.
 * @param onNavigateToRegister Callback ejecutado al pulsar el enlace de registro.
 * @param onNavigateToOnboarding Callback ejecutado tras login exitoso cuando se requiere onboarding.
 * @param ViewModel ViewModel de login inyectado por Hilt.
 * @param snackbarViewModel ViewModel para mostrar mensajes snackbar.
 */
@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    snackbarViewModel: GlobalSnackbarViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.loginState.collectAsStateWithLifecycle()
    val isLoading = uiState is LoginUIState.Loading
    val errorMessage = if (uiState is LoginUIState.Error) (uiState as LoginUIState.Error).message else null
    val isError = uiState is LoginUIState.Error

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is LoginEvent.NavigateToHome -> onNavigateToHome()
                is LoginEvent.NavigateToOnboarding -> onNavigateToOnboarding()

            }
        }
    }

    val handleGoogleSignIn: () -> Unit = {
        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(request = request, context = context)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    snackbarViewModel.showMessage(context.getString(R.string.login_google_token_error))
                    Log.e("GOOGLE_LOGIN", "Credential no es GoogleIdTokenCredential: ${credential?.javaClass?.simpleName}")
                }
            } catch (e: GetCredentialException) {
                val msg = e.errorMessage?.toString() ?: e.message ?: "Error al obtener credenciales"
                snackbarViewModel.showMessage(msg)
                Log.e("GOOGLE_LOGIN", "Error de CredentialManager: ${e.javaClass.simpleName} - $msg")
            } catch (e: Exception) {
                snackbarViewModel.showMessage(e.message ?: "Error de Google Sign-In")
                Log.e("GOOGLE_LOGIN", "Error general: ${e.message}")
            }
        }
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Icon(
                painter = painterResource(id = R.drawable.logo_new),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.login_welcome_back),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Gray300
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Primary.copy(alpha = 0.1f),
                        spotColor = Primary.copy(alpha = 0.2f)
                    )
                    .background(
                        color = Surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    // Email TextField
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp),
                        placeholder = { Text(stringResource(R.string.login_email_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Gray300
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Gray700,
                            focusedPlaceholderColor = Gray300,
                            unfocusedPlaceholderColor = Gray300,
                            focusedLeadingIconColor = Primary,
                            unfocusedLeadingIconColor = Gray300,
                            cursorColor = Primary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password TextField
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp),
                        placeholder = { Text(stringResource(R.string.login_password_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Gray300
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) stringResource(R.string.cd_hide_password) else stringResource(R.string.cd_show_password),
                                    tint = Gray300
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Gray700,
                            focusedPlaceholderColor = Gray300,
                            unfocusedPlaceholderColor = Gray300,
                            focusedLeadingIconColor = Primary,
                            unfocusedLeadingIconColor = Gray300,
                            focusedTrailingIconColor = Primary,
                            unfocusedTrailingIconColor = Gray300,
                            cursorColor = Primary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        isError = isError,
                        supportingText = if (isError) { { Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error) } } else null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Forgot password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = stringResource(R.string.login_forgot_password),
                            color = Secondary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { /* TODO: Navigate to forgot password */ }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Log In Button
                    Button(
                        onClick = { viewModel.login(LoginRequest(email, password)) },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.White,
                            disabledContainerColor = Primary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.login_button),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Gray700
                )
                Text(
                    text = stringResource(R.string.login_or_continue_with),
                    color = Gray300,
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Gray700
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Google Button
                OutlinedButton(
                    onClick = handleGoogleSignIn,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Gray700),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.login_google_button),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Guest Button
                OutlinedButton(
                    onClick = {
                        viewModel.startGuestSession()
                        onNavigateToHome()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Secondary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Secondary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.login_guest_button),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    color = Gray300,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.login_sign_up),
                    color = Secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
