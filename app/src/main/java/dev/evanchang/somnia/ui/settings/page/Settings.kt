package dev.evanchang.somnia.ui.settings.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.evanchang.somnia.NavApiSettingsScreen
import dev.evanchang.somnia.ui.settings.composable.SettingsDirectory
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavController) {
    SettingsScaffold(
        navController = navController,
        title = "Settings",
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SettingsDirectory(
                onClick = { navController.navigate(NavApiSettingsScreen) }, text = "API"
            )
        }
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    Settings(navController = rememberNavController())
}