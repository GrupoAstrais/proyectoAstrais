package com.mm.astraisandroid.ui.features.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.R
import com.mm.astraisandroid.ui.features.profile.UserViewModel
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.ui.theme.Gray700
import com.mm.astraisandroid.ui.theme.Primary
import com.mm.astraisandroid.ui.theme.Secondary
import com.mm.astraisandroid.ui.theme.Surface
import com.mm.astraisandroid.ui.theme.Tertiary

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    val userState by userViewModel.state.collectAsStateWithLifecycle()
    val errorMessage = userState.error

    var selectedLanguage by remember { mutableStateOf(userState.user?.language ?: "ESP") }

    LaunchedEffect(userState.user?.language) {
        userState.user?.language?.let { selectedLanguage = it }
    }

    val usernameLength = username.length
    val maxUsernameLength = 20
    val isUsernameValid = username.length >= 3 && username.length <= maxUsernameLength
    val isUsernameTooLong = username.length > maxUsernameLength
    val canSubmit = isUsernameValid && !userState.isLoading && userState.user != null

    LaunchedEffect(Unit) {
        userViewModel.fetchUser()
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
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.onboarding_subtitle),
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
                    // Username TextField
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            if (it.length <= maxUsernameLength) {
                                username = it
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp),
                        placeholder = { Text(stringResource(R.string.onboarding_username_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isUsernameValid) Tertiary else Gray300
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isUsernameValid) Tertiary else if (isUsernameTooLong || (username.isNotEmpty() && username.length < 3)) MaterialTheme.colorScheme.error else Primary,
                            unfocusedBorderColor = Gray700,
                            focusedPlaceholderColor = Gray300,
                            unfocusedPlaceholderColor = Gray300,
                            focusedLeadingIconColor = if (isUsernameValid) Tertiary else Primary,
                            unfocusedLeadingIconColor = Gray300,
                            cursorColor = Primary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        isError = username.isNotEmpty() && username.length < 3,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (username.isNotEmpty() && username.length < 3) stringResource(R.string.onboarding_username_min_chars) else "",
                                    color = if (username.isNotEmpty() && username.length < 3) MaterialTheme.colorScheme.error else Color.Transparent,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "$usernameLength/$maxUsernameLength",
                                    color = if (isUsernameTooLong) MaterialTheme.colorScheme.error else if (isUsernameValid) Tertiary else Gray300,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Language Section
                    Text(
                        text = stringResource(R.string.onboarding_language_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LanguageChip(
                            label = stringResource(R.string.language_spanish),
                            code = "ESP",
                            isSelected = selectedLanguage == "ESP",
                            onClick = { selectedLanguage = "ESP" },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageChip(
                            label = stringResource(R.string.language_english),
                            code = "ENG",
                            isSelected = selectedLanguage == "ENG",
                            onClick = { selectedLanguage = "ENG" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Secondary.copy(alpha = 0.1f),
                        spotColor = Secondary.copy(alpha = 0.15f)
                    )
                    .background(
                        color = Surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.onboarding_profile_preview),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Secondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (username.isNotBlank()) username else stringResource(R.string.onboarding_username_preview_placeholder),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = Secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (selectedLanguage == "ESP") stringResource(R.string.language_spanish) else stringResource(R.string.language_english),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray300
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Start Adventure Button
            Button(
                onClick = {
                    userViewModel.updateProfile(username, selectedLanguage, onFinish)
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White,
                    disabledContainerColor = Gray700,
                    disabledContentColor = Gray300
                )
            ) {
                if (userState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.onboarding_start_button),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LanguageChip(
    label: String,
    code: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) Primary else Gray700,
        label = "borderColor"
    )
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) Primary.copy(alpha = 0.15f) else Color.Transparent,
        label = "backgroundColor"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        label = "scale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBackgroundColor)
            .border(1.5.dp, animatedBorderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 16.dp)
            .scale(animatedScale),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Gray300,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
