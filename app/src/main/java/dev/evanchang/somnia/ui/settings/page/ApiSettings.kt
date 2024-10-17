package dev.evanchang.somnia.ui.settings.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettings(navController: NavController) {
    SettingsScaffold(
        navController = navController,
        title = "API Settings",
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "API Settings", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview
@Composable
private fun ApiSettingsPreview() {
    ApiSettings(navController = rememberNavController())
}