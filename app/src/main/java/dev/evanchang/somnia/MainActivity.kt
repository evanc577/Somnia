package dev.evanchang.somnia

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.datastore.dataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            val appSettings by dataStore.data.collectAsStateWithLifecycle(initialValue = null)

            // Login/logout the current user if app settings have changed
            LaunchedEffect(appSettings) {
                val accountSettings = appSettings?.accountSettings?.get(appSettings?.activeUser)
                if (accountSettings != null) {
                    RedditHttpClient.login(accountSettings)
                } else {
                    RedditHttpClient.logout()
                }

                val userAgent = appSettings?.apiSettings?.redditUserAgent
                RedditHttpClient.setUserAgent(userAgent)
            }

            SomniaTheme {
                NavigationRoot()
            }
        }
    }
}