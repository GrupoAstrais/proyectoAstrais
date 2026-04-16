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

// https://m3.material.io/components/text-fields/overview
@Composable
fun AuthTextField(
    value: String,                                  // Texto que muestra el campo en este momento
    onValueChange: (String) -> Unit,                // Lambda que se llama cada vez que el usuario escribe o borra un caracter
    label: String,                                  /* Texto flotante que aparece dentro del campo vacío como placeholder,
                                                       sube arriba cuando el campo está enfocado o tiene texto */
    modifier: Modifier = Modifier,                  // Permite que cuando se llama la función se pueda añadir modificadores extra
    keyboardType: KeyboardType = KeyboardType.Text, // Le dice al sistema operativo que teclado mostrar
    imeAction: ImeAction = ImeAction.Next,          // Controla el boton de acción del teclado
    isPassword: Boolean = false,                    // Boolean para aplicar el transform de contraseñas
    isError: Boolean = false,                       // Boolean que le dice al textfield que esta en estado de error. Cambia colores
    supportingText: String? = null                  // Texto pequeño que aparece debajo del campo, para errores basicamente
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