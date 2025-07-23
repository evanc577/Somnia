package dev.evanchang.somnia

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.dataStore
import dev.evanchang.somnia.api.RedditHttpClient
import dev.evanchang.somnia.appSettings.AppSettingsSerializer
import dev.evanchang.somnia.navigation.NavigationRoot
import dev.evanchang.somnia.ui.theme.SomniaTheme

val Context.dataStore by dataStore("settings.json", AppSettingsSerializer)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settings by context.dataStore.data.collectAsState(null)
            var settingsLoaded by remember { mutableStateOf(false) }

            // Login/logout the current user if app settings have changed
            LaunchedEffect(settings) {
                val settings = settings
                if (settings == null) {
                    return@LaunchedEffect
                }

                // Set active user
                val accountSettings = settings.accountSettings.get(settings.activeUser)
                if (accountSettings != null) {
                    RedditHttpClient.login(accountSettings)
                } else {
                    RedditHttpClient.logout()
                }

                // Set user agent
                RedditHttpClient.setUserAgent(settings.apiSettings.redditUserAgent)

                // Account settings have been loaded at least once
                settingsLoaded = true
            }

            SomniaTheme {
                if (settingsLoaded) {
                    NavigationRoot()
                }
            }
        }
    }
}