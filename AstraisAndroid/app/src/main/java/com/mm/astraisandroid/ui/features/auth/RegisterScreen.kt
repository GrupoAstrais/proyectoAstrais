package com.mm.astraisandroid.ui.features.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.data.api.RegisterRequest
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }

    val uiState by viewModel.registerState.collectAsStateWithLifecycle()

    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val canSubmit = email.isNotEmpty()
            && password.isNotEmpty()
            && !passwordMismatch
            && acceptedTerms
            && uiState !is RegisterUIState.Loading

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is RegisterEvent.NavigateToLogin -> {
                    onRegisterSuccess()
                }
                is RegisterEvent.ShowToast -> {
                    // TODO: Snackbar
                }
            }
        }
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Título
            Text(
                text = "CREATE YOUR\nACCOUNT",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                lineHeight = 34.sp,
                fontFamily = FontFamily.Monospace
            )

            // Formulario
            Column(modifier = Modifier.fillMaxWidth()) {
                if (uiState is RegisterUIState.CodeSent) {
                    Text(
                        text = "Ingresa el código enviado a tu correo",
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    AuthTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        label = "Código de verificación",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { viewModel.verifyCode(verificationCode) },
                        enabled = verificationCode.length >= 6 && uiState !is RegisterUIState.Loading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D0D0D),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF0D0D0D).copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Verificar Cuenta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    keyboardType = KeyboardType.Email
                )

                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true
                )

                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    isError = passwordMismatch,
                    supportingText = if (passwordMismatch) "Passwords do not match" else null
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Términos y condiciones
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { acceptedTerms = !acceptedTerms }
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkmarkColor = Color.Black,
                            checkedColor = Color.White,
                            uncheckedColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            ) {
                                append("I agree to the ")
                            }
                            withStyle(SpanStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
                                append("Terms & Conditions")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Error del servidor
                    if (uiState is RegisterUIState.Error) {
                        Text(
                            text = (uiState as RegisterUIState.Error).message,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                Button(
                    onClick = {
                        viewModel.register(RegisterRequest(email.split("@")[0], email, password, "ESP"))
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D0D0D),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF0D0D0D).copy(alpha = 0.5f)
                    )
                ) {
                    if (uiState is RegisterUIState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Already have an account? ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Text(
                    "Log In",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}}