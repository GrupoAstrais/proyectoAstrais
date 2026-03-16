package com.mm.astraisandroid.ui.auth.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mm.astraisandroid.api.LoginRequest
import com.mm.astraisandroid.api.LoginUIState
import com.mm.astraisandroid.api.LoginViewModel
import com.mm.astraisandroid.api.RegisterRequest
import com.mm.astraisandroid.ui.auth.components.AuthBackground
import com.mm.astraisandroid.ui.auth.components.AuthTextField


@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    // remember sirve para guardar el valor entre recomposiciones
    // sino cada vez que Compose redibuja la pantalla, email volvería a ser ""
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // es un StateFlow en el ViewModel, es como un useState
    val uiState by viewModel.loginState.collectAsStateWithLifecycle()

    val isError = uiState is LoginUIState.LoginError

    // Ejecuta el bloque cuando uiState cambia, un useEffect vaya
    LaunchedEffect(uiState) {
        if (uiState is LoginUIState.LoginSuccess) {
            onNavigateToHome()
            viewModel.resetState()
        }
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Titulo
            Text(
                text = "LOG INTO YOUR\nACCOUNT",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                lineHeight = 34.sp,
                fontFamily = FontFamily.Monospace
            )

            // Formulario
            Column(modifier = Modifier.fillMaxWidth()) {
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
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    isError = isError,
                    supportingText =    if (isError)
                                        (uiState as LoginUIState.LoginError).message
                                        else null
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.login(LoginRequest(email, password)) },
                    enabled = uiState !is LoginUIState.Loading,
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
                    if (uiState is LoginUIState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.testRegister(RegisterRequest("Test", "test@test.com", "test", "ESP")) },
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
                    Text("Register test")
                }
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Don't have an account? ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Text(
                    "Sign Up",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}
