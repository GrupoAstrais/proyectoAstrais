package com.mm.astraisandroid.ui.features.auth

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.R
import com.mm.astraisandroid.data.api.RegisterRequest
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.ui.theme.Gray700
import com.mm.astraisandroid.ui.theme.Primary
import com.mm.astraisandroid.ui.theme.Secondary
import com.mm.astraisandroid.ui.theme.Surface
import kotlinx.coroutines.flow.collectLatest

/**
 * Pantalla de registro de nuevos usuarios con verificación por email.
 *
 * Presenta un formulario de dos fases: primero recopila email, contraseña y aceptación
 * de términos; tras el envío, muestra un campo para introducir el código de verificación
 * recibido por email. Valida formato de email, coincidencia de contraseñas y longitud mínima.
 *
 * @param onNavigateToLogin Callback ejecutado al pulsar el enlace de inicio de sesión.
 * @param onRegisterSuccess Callback ejecutado tras registro y verificación exitosos.
 * @param onNavigateToOnboarding Callback ejecutado tras verificación exitosa del email.
 * @param viewModel ViewModel de registro inyectado por Hilt.
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.registerState.collectAsStateWithLifecycle()
    val isLoading = uiState is RegisterUIState.Loading
    val isCodeSent = uiState is RegisterUIState.CodeSent
    val errorMessage = if (uiState is RegisterUIState.Error) (uiState as RegisterUIState.Error).message else null
    val isError = uiState is RegisterUIState.Error

    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val canSubmit = email.isNotEmpty()
            && isEmailValid
            && password.isNotEmpty()
            && !passwordMismatch
            && acceptedTerms
            && !isLoading

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is RegisterEvent.NavigateToLogin -> onRegisterSuccess()
                is RegisterEvent.NavigateToOnboarding -> onNavigateToOnboarding()

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
                text = if (isCodeSent) stringResource(R.string.register_verify_email_title) else stringResource(R.string.register_create_account_title),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isCodeSent) stringResource(R.string.register_verify_subtitle) else stringResource(R.string.register_subtitle),
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
                    if (isCodeSent) {
                        // Verification Code Section
                        OutlinedTextField(
                            value = verificationCode,
                            onValueChange = { verificationCode = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp),
                            placeholder = { Text(stringResource(R.string.register_verification_code_placeholder)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = Gray300
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
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
                                cursorColor = Primary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            isError = isError,
                            supportingText = if (isError) { { Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error) } } else null
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.verifyCode(verificationCode) },
                            enabled = verificationCode.length >= 6 && !isLoading,
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
                                    text = stringResource(R.string.register_verify_button),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        // Registration Form
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp),
                            placeholder = { Text(stringResource(R.string.register_email_placeholder)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Email,
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
                            isError = email.isNotEmpty() && !isEmailValid,
                            supportingText = if (email.isNotEmpty() && !isEmailValid) { { Text("Formato de email inválido", color = MaterialTheme.colorScheme.error) } } else null,
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

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp),
                            placeholder = { Text(stringResource(R.string.register_password_placeholder)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Gray300
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible)
                                            Icons.Filled.Visibility
                                        else
                                            Icons.Filled.VisibilityOff,
                                        contentDescription = if (passwordVisible) stringResource(R.string.cd_hide_password) else stringResource(R.string.cd_show_password),
                                        tint = Gray300
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
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
                                focusedTrailingIconColor = Primary,
                                unfocusedTrailingIconColor = Gray300,
                                cursorColor = Primary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp),
                            placeholder = { Text(stringResource(R.string.register_confirm_password_placeholder)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Gray300
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible)
                                            Icons.Filled.Visibility
                                        else
                                            Icons.Filled.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) stringResource(R.string.cd_hide_password) else stringResource(R.string.cd_show_password),
                                        tint = Gray300
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                            isError = passwordMismatch,
                            supportingText = if (passwordMismatch) { { Text(stringResource(R.string.register_password_mismatch), color = MaterialTheme.colorScheme.error) } } else null
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Terms and Conditions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { acceptedTerms = !acceptedTerms }
                        ) {
                            Checkbox(
                                checked = acceptedTerms,
                                onCheckedChange = { acceptedTerms = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Primary,
                                    uncheckedColor = Gray300,
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                buildAnnotatedString {
                                    append(stringResource(R.string.register_terms_prefix))
                                    pushStyle(SpanStyle(color = Secondary, fontWeight = FontWeight.Bold))
                                    append(stringResource(R.string.register_terms_conditions))
                                    pop()
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray300
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Server Error
                        if (isError) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Create Account Button
                        Button(
                            onClick = {
                                viewModel.register(RegisterRequest("NUEVO_USUARIO", email, password, "ESP"))
                            },
                            enabled = canSubmit,
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
                                    text = stringResource(R.string.register_create_account_button),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
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
                    text = stringResource(R.string.register_already_have_account),
                    color = Gray300,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.register_log_in),
                    color = Secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
