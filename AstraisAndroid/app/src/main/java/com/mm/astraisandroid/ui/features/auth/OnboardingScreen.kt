package com.mm.astraisandroid.ui.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.ui.features.profile.UserViewModel

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("ESP") }
    val userState by userViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        userViewModel.fetchUser()
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "CONFIGURA TU\nPERFIL",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                lineHeight = 34.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "Casi terminamos. Elige cómo te verán los demás y tu idioma preferido.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthTextField(
                value = username,
                onValueChange = { username = it },
                label = "Nombre de Usuario"
            )

            Text(
                text = "Idioma",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LanguageOption(
                    label = "Español",
                    code = "ESP",
                    isSelected = selectedLanguage == "ESP",
                    onClick = { selectedLanguage = "ESP" },
                    modifier = Modifier.weight(1f)
                )
                LanguageOption(
                    label = "English",
                    code = "ENG",
                    isSelected = selectedLanguage == "ENG",
                    onClick = { selectedLanguage = "ENG" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (userState.error != null) {
                Text(
                    text = userState.error!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = {
                    userViewModel.updateProfile(username, selectedLanguage, onFinish)
                },
                enabled = username.length >= 3 && !userState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.3f)
                )
            ) {
                if (userState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text("Empezar Aventura", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LanguageOption(
    label: String,
    code: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
