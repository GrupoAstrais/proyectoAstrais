package com.mm.astraisandroid.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mm.astraisandroid.data.api.BASE_URL
import android.util.Log

@Composable
fun LottiePetRenderer(assetRef: String, modifier: Modifier = Modifier) {
    val lottieUrl = "$BASE_URL/assets/pets/$assetRef"

    // rememberLottieComposition devuelve un "Result" que nos chiva si hay error
    val compositionResult = rememberLottieComposition(LottieCompositionSpec.Url(lottieUrl))
    val composition by compositionResult
    Log.d("TIENDA", "Error Lottie:\n${compositionResult.error?.message}")
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (compositionResult.isLoading) {
            CircularProgressIndicator(
                color = Color(0xFFC172FF),
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else if (compositionResult.error != null) {
            Text(
                text = "Error Lottie:\n${compositionResult.error?.message}",
                color = Color.Red,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        } else {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}