package org.thesis.project.Components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class MyColor(val hex: Long)

@Immutable
data class AppColors(
    val primaryBlue: Color,
    val secondaryBlue: Color,
    val thirdlyBlue: Color,

    val primaryRed: Color,
    val secondaryRed: Color,
    val thirdlyRed: Color,

    val primaryGray: Color,
    val primaryDarkGray: Color,

    val textColor: Color,
    val oppositeTextColor: Color,
    val backgroundColor: Color,
    val secondaryBackgroundColor: Color,

    val cardColor: Color,

    val buttonColor: Color,
    val buttonTextColor: Color,

    // add other colors as needed
)

val LightColors = AppColors(
    primaryBlue = Color(0xFF0050A0), //Dark blue
    secondaryBlue = Color(0xFF80A7D0), //blue
    thirdlyBlue = Color(0xFFDCE7F6),
    primaryRed = Color(0xFFF05933),
    secondaryRed = Color(0xFFF59076),
    thirdlyRed = Color(0xFFFCDED6),

    primaryGray = Color(0xFFAEADAD),
    primaryDarkGray = Color(0xFF4A4A4A),
    textColor = Color.Black,
    oppositeTextColor = Color.White,
    backgroundColor = Color.White,
    secondaryBackgroundColor = Color.LightGray,
    cardColor = Color(0xFFF0F4F8),

    buttonColor = Color(0xFF0050A0),
    buttonTextColor = Color.White,

    // other light colors...
)

val DarkColors = AppColors(
    primaryBlue = Color(0xFF0050A0), //Dark blue
    secondaryBlue = Color(0xFF80A7D0), //blue
    thirdlyBlue = Color(0xFFDCE7F6),
    primaryRed = Color(0xFFF05933),
    secondaryRed = Color(0xFFF59076),
    thirdlyRed = Color(0xFFFCDED6),

    primaryGray = Color(0xFFAEADAD),
    primaryDarkGray = Color(0xFF4A4A4A),
    textColor = Color.Black,
    oppositeTextColor = Color.White,

    backgroundColor = Color.White,
    secondaryBackgroundColor = Color.LightGray,
    cardColor = Color(0xFFF0F4F8),

    buttonColor = Color(0xFF0050A0),
    buttonTextColor = Color.White,

    )

fun AppColors.toMaterialColors(darkTheme: Boolean): Colors {
    return if (darkTheme) {
        darkColors(
            primary = primaryBlue,
            secondary = secondaryBlue,
            background = backgroundColor,
            surface = cardColor,
            onPrimary = textColor,
            onSecondary = oppositeTextColor,
            onBackground = textColor,
            onSurface = textColor,
            error = primaryRed
        )
    } else {
        lightColors(
            primary = primaryBlue,
            secondary = secondaryBlue,
            background = backgroundColor,
            surface = cardColor,
            onPrimary = textColor,
            onSecondary = oppositeTextColor,
            onBackground = textColor,
            onSurface = textColor,
            error = primaryRed
        )
    }
}

val LocalAppColors = staticCompositionLocalOf { LightColors }

@Composable
fun regionVasterbottenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val customColors = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(LocalAppColors provides customColors) {
        MaterialTheme(
            colors = customColors.toMaterialColors(darkTheme),
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes,
            content = content
        )
    }
}