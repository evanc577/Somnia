package dev.evanchang.somnia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.evanchang.somnia.Ui.Scaffold.MainScaffold
import dev.evanchang.somnia.Ui.Settings.Settings
import dev.evanchang.somnia.Ui.Theme.SomniaTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            SomniaTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = HomeScreen,
                ) {
                    composable<HomeScreen> {
                        MainScaffold(navController = navController)
                    }
                    composable<SettingsScreen> {
                        Settings(navController = navController)
                    }
                }
            }
        }
    }
}

@Serializable
object HomeScreen

@Serializable
object SettingsScreen