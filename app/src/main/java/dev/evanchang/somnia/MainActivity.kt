package dev.evanchang.somnia

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.dataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import dev.evanchang.somnia.appSettings.AppSettingsSerializer
import dev.evanchang.somnia.ui.scaffold.MainScaffold
import dev.evanchang.somnia.ui.settings.page.ApiSettings
import dev.evanchang.somnia.ui.settings.page.Settings
import dev.evanchang.somnia.ui.theme.SomniaTheme
import kotlinx.serialization.Serializable

val Context.dataStore by dataStore("settings.json", AppSettingsSerializer)

@Serializable
object NavHomeScreen

@Serializable
object NavSettings
@Serializable
object NavSettingsScreen
@Serializable
object NavApiSettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            SomniaTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavHomeScreen,
                ) {
                    composable<NavHomeScreen> {
                        MainScaffold(navController = navController)
                    }
                    navigation<NavSettings>(startDestination = NavSettingsScreen) {
                        composable<NavSettingsScreen> {
                            Settings(navController = navController)
                        }
                        composable<NavApiSettingsScreen> {
                            ApiSettings(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
