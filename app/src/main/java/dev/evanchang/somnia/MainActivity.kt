package dev.evanchang.somnia

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.dataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.evanchang.somnia.appSettings.AppSettingsSerializer
import dev.evanchang.somnia.ui.redditscreen.Home
import dev.evanchang.somnia.ui.redditscreen.homeDestination
import dev.evanchang.somnia.ui.settings.navigateToSettings
import dev.evanchang.somnia.ui.settings.settingsNavigation
import dev.evanchang.somnia.ui.theme.SomniaTheme

val Context.dataStore by dataStore("settings.json", AppSettingsSerializer)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            SomniaTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Home,
                ) {
                    homeDestination(onNavigateToSettings = {
                        navController.navigateToSettings()
                    })
                    settingsNavigation(navController)
                }
            }
        }
    }
}
