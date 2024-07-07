package dev.evanchang.somnia

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.evanchang.somnia.api.reddit.SubredditSubmissionsViewModel
import dev.evanchang.somnia.ui.composables.SubmissionList
import dev.evanchang.somnia.ui.theme.SomniaTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel by viewModels<SubredditSubmissionsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            SomniaTheme {
                Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceDim)) {
                    Spacer(
                        modifier = Modifier.windowInsetsTopHeight(WindowInsets.systemBars)
                    )
                    SubmissionList(submissions = mainViewModel.submissions)
                }
            }
        }
    }
}