package dev.evanchang.somnia.ui.settings.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.evanchang.somnia.ui.settings.composable.SettingsDirectory
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit, onNavigateToApiSettings: () -> Unit
) {
    SettingsScaffold(
        title = "Settings",
        onNavigateBack = onNavigateBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SettingsDirectory(
                onClick = { onNavigateToApiSettings() }, text = "API"
            )
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(onNavigateBack = {}, onNavigateToApiSettings = {})
}