package dev.evanchang.somnia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import dev.evanchang.somnia.api.reddit.SubredditSubmissionsViewModel
import dev.evanchang.somnia.ui.composables.SubmissionList

class MainActivity : ComponentActivity() {
    private val mainViewModel: SubredditSubmissionsViewModel = SubredditSubmissionsViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
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
