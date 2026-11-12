package com.anurag.activestudent.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgDark = Color(0xFF0B0B0B)
val BgSurface = Color(0xFF121213)
val CardBg = Color(0xFF161617)
val CardBorder = Color(0x14FFFFFF)
val AccentOrange = Color(0xFFFF9A2C)
val AccentOrangeSoft = Color(0x26FF9A2C)
val GreenSuccess = Color(0xFF22C55E)
val GreenSoft = Color(0x2222C55E)
val RedDanger = Color(0xFFEF4444)
val RedSoft = Color(0x22EF4444)
val YellowWarn = Color(0xFFEAB308)
val YellowSoft = Color(0x22EAB308)
val BlueInfo = Color(0xFF3B82F6)
val TextMain = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF9A9A9A)
val InputBg = Color(0xFF1E1E20)

private val DarkScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = Color.Black,
    secondary = AccentOrange,
    onSecondary = Color.Black,
    background = BgDark,
    onBackground = TextMain,
    surface = CardBg,
    onSurface = TextMain,
    surfaceVariant = InputBg,
    onSurfaceVariant = TextMuted,
    error = RedDanger,
    onError = TextMain,
    outline = CardBorder
)

@Composable
fun ActiveStudentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        content = content
    )
}

