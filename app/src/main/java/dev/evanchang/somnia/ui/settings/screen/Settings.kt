package dev.evanchang.somnia.ui.settings.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.compose.settings.ui.SettingsMenuLink
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToApiSettings: () -> Unit,
    onNavigateToAccountSettings: () -> Unit,
) {
    SettingsScaffold(
        title = "Settings",
        onNavigateBack = onNavigateBack,
    ) {
        SettingsMenuLink(title = { Text(text = "Account") },
            onClick = { onNavigateToAccountSettings() },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle, contentDescription = "account icon"
                )
            })
        SettingsMenuLink(title = { Text(text = "API") },
            onClick = { onNavigateToApiSettings() },
            icon = { Icon(imageVector = Icons.Default.Api, contentDescription = "API icon") })
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        onNavigateBack = {},
        onNavigateToApiSettings = {},
        onNavigateToAccountSettings = {})
}