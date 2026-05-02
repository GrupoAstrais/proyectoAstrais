package com.mm.astraisandroid.ui.features.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Campo de texto estilizado para las pantallas de autenticación.
 *
 * Envuelve un [TextField] de Material 3 con una paleta de colores personalizada
 * adaptada al tema oscuro de Astrais. Soporta texto de contraseña, indicadores de
 * error y texto de ayuda.
 *
 * @param value Texto actual del campo.
 * @param onValueChange Callback invocado en cada cambio de texto.
 * @param label Etiqueta flotante que actúa como placeholder.
 * @param modifier Modificadores adicionales de Compose.
 * @param keyboardType Tipo de teclado que debe mostrar el sistema operativo.
 * @param imeAction Acción del botón principal del teclado virtual.
 * @param isPassword Si es `true`, oculta el texto introducido.
 * @param isError Si es `true`, resalta el campo en color de error.
 * @param supportingText Texto auxiliar mostrado debajo del campo, útil para mensajes de error.
 *
 * @see <a href="https://m3.material.io/components/text-fields/overview">Material Text Fields</a>
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        visualTransformation = if (isPassword) PasswordVisualTransformation()
        else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        supportingText = if (supportingText != null) {
            { Text(supportingText, color = Color(0xFFFF6B6B)) }
        } else null,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor    = Color.Transparent,               // El fondo del campo cuando está enfocado
            unfocusedContainerColor  = Color.Transparent,               // El fondo del campo cuando no está enfocado
            errorContainerColor      = Color.Transparent,               // El fondo cuando isError = true
            focusedIndicatorColor    = Color.White,                     // Linea inferior cuando el campo esta enfocado
            unfocusedIndicatorColor  = Color.White.copy(alpha = 0.5f),  // Linea inferior cuando el campo no esta enfocado
            errorIndicatorColor      = Color(0xFFFF6B6B),        // Linea inferior cuando isError = true
            focusedTextColor         = Color.White,                     // El color del texto que escribe el usuario
            unfocusedTextColor       = Color.White,                     // El color del texto que escribe el usuario
            cursorColor              = Color.White,                     // El color del cursor mientras el usuario escribe
            focusedLabelColor        = Color.White,                     // El color del texto del label flotante cuando esta enfocado
            unfocusedLabelColor      = Color.White.copy(alpha = 0.6f),  // El color del texto del label flotante cuando no esta enfocado
            errorLabelColor          = Color(0xFFFF6B6B),        /**/
            errorTextColor           = Color.White,                     /*  Los colores de label, texto y cursor cuando isError = true */
            errorCursorColor         = Color(0xFFFF6B6B),        /* */
        )
    )
}