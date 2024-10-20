package dev.evanchang.somnia

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.datastore.dataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.evanchang.somnia.api.RedditHttpClient
import dev.evanchang.somnia.appSettings.AppSettings
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

            // Login/logout the current user if app settings have changed
            val appSettings by dataStore.data.collectAsStateWithLifecycle(initialValue = AppSettings())
            LaunchedEffect(appSettings) {
                val accountSettings = appSettings.accountSettings.get(appSettings.activeUser)
                if (accountSettings != null) {
                    RedditHttpClient.login(accountSettings)
                } else {
                    RedditHttpClient.logout()
                }
            }
        }
    }
}
