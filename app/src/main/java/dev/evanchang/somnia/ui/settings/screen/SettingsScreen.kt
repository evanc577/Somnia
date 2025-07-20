package dev.evanchang.somnia.ui.settings.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavBackStack
import com.alorma.compose.settings.ui.SettingsMenuLink
import dev.evanchang.somnia.navigation.Nav
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold

@Composable
fun SettingsScreen(
    backStack: NavBackStack,
) {
    SettingsScaffold(
        title = "Settings",
        backStack = backStack,
    ) {
        SettingsMenuLink(
            title = { Text(text = "Account") },
            onClick = { backStack.add(Nav.Settings.Account) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle, contentDescription = "account icon"
                )
            },
        )
        SettingsMenuLink(
            title = { Text(text = "API") },
            onClick = { backStack.add(Nav.Settings.Api) },
            icon = { Icon(imageVector = Icons.Default.Api, contentDescription = "API icon") },
        )
        SettingsMenuLink(
            title = { Text(text = "General") },
            onClick = { backStack.add(Nav.Settings.General) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings, contentDescription = "settings icon"
                )
            },
        )
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(NavBackStack())
}