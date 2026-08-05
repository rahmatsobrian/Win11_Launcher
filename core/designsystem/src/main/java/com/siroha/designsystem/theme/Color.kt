package com.siroha.designsystem.theme

import androidx.compose.ui.graphics.Color

// Windows 11 "Mica" light surfaces
val MicaLightBase = Color(0xFFF3F3F3)
val MicaLightElevated = Color(0xFFFAFAFA)
val AcrylicLightOverlay = Color(0x99FFFFFF)

// Windows 11 "Mica" dark surfaces
val MicaDarkBase = Color(0xFF202020)
val MicaDarkElevated = Color(0xFF2C2C2C)
val AcrylicDarkOverlay = Color(0x99202020)

// Default accent (Windows 11 default blue)
val AccentBlue = Color(0xFF0078D4)
val AccentBlueLight = Color(0xFF60CDFF)
val AccentBlueDark = Color(0xFF003966)

// Taskbar / Start menu chrome — deliberately lighter/darker than the
// desktop background (MicaDarkBase/MicaLightBase) rather than the same
// hue at different alpha, so the taskbar reads as a distinct surface
// instead of visually merging into the desktop behind it.
val TaskbarLight = Color(0xF2FFFFFF)
val TaskbarDark = Color(0xF23A3A3A)

val OnMicaLight = Color(0xFF1B1B1B)
val OnMicaDark = Color(0xFFE6E6E6)

val DividerLight = Color(0x1F000000)
val DividerDark = Color(0x1FFFFFFF)

val ErrorRed = Color(0xFFC42B1C)
val SuccessGreen = Color(0xFF0F7B0F)
