package nerd.tuxmobil.fahrplan.congress.designsystem.typography

import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography as Material3Typography

internal fun createEventFahrplanTypography(typography: Material3Typography): Typography {
    return Typography(
        displayLarge = typography.displayLarge,
        displayMedium = typography.displayMedium,
        displaySmall = typography.displaySmall,
        headlineLarge = typography.headlineLarge,
        headlineMedium = typography.headlineMedium,
        headlineSmall = typography.headlineSmall,
        titleLarge = typography.titleLarge,
        titleMedium = typography.titleMedium,
        titleSmall = typography.titleSmall,
        bodyLarge = typography.bodyLarge,
        bodyMedium = typography.bodyMedium,
        bodySmall = typography.bodySmall,
        labelLarge = typography.labelLarge,
        labelMedium = typography.labelMedium,
        labelSmall = typography.labelSmall,

        preferenceTitle = typography.titleLarge.copy(fontSize = 16.sp),
    )
}
