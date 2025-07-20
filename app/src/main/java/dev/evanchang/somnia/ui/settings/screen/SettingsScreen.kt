package dev.evanchang.somnia.ui.settings.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.alorma.compose.settings.ui.SettingsMenuLink
import dev.evanchang.somnia.navigation.Nav
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold
import kotlinx.serialization.Serializable

@Composable
fun SettingsScreen(
    onBack: (Int) -> Unit,
    onNavigate: (Nav) -> Unit,
) {
    SettingsScaffold(
        title = "Settings",
        onBack = onBack,
    ) {
        SettingsMenuLink(
            title = { Text(text = "Account") },
            onClick = { onNavigate(Nav.Settings(SettingsNavKey.Account(AccountSettingsNavKey.TopLevel))) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle, contentDescription = "account icon"
                )
            },
        )
        SettingsMenuLink(
            title = { Text(text = "API") },
            onClick = { onNavigate(Nav.Settings(SettingsNavKey.Api)) },
            icon = { Icon(imageVector = Icons.Default.Api, contentDescription = "API icon") },
        )
        SettingsMenuLink(
            title = { Text(text = "General") },
            onClick = { onNavigate(Nav.Settings(SettingsNavKey.General)) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings, contentDescription = "settings icon"
                )
            },
        )
    }
}

fun SettingsNav(
    key: SettingsNavKey,
    onBack: (Int) -> Unit,
    onNavigate: (Nav) -> Unit,
): NavEntry<NavKey> {
    return when (key) {
        is SettingsNavKey.TopLevel -> NavEntry(key) {
            SettingsScreen(
                onBack = onBack,
                onNavigate = onNavigate,
            )
        }

        is SettingsNavKey.Api -> NavEntry(key) {
            ApiSettingsScreen(
                onBack = onBack,
            )
        }

        is SettingsNavKey.Account -> AccountSettingsNav(
            key = key.key,
            onBack = onBack,
            onNavigate = onNavigate,
        )

        is SettingsNavKey.General -> NavEntry(key) {
            GeneralSettingsScreen(
                onBack = onBack,
            )
        }
    }
}

@Serializable
sealed class SettingsNavKey : NavKey {
    @Serializable
    data object TopLevel : SettingsNavKey()

    @Serializable
    data object General : SettingsNavKey()

    @Serializable
    data class Account(val key: AccountSettingsNavKey) : SettingsNavKey()

    @Serializable
    data object Api : SettingsNavKey()
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        onBack = {},
        onNavigate = {},
    )
}