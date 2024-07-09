package dev.evanchang.somnia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import dev.evanchang.somnia.ui.scaffold.MainScaffold
import dev.evanchang.somnia.ui.submissions.Submissions
import dev.evanchang.somnia.ui.submissions.SubmissionsViewModel
import dev.evanchang.somnia.ui.theme.SomniaTheme

class MainActivity : ComponentActivity() {
    private val submissionsViewModel by viewModels<SubmissionsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            SomniaTheme {
                MainScaffold {
                    Submissions(submissionsViewModel = submissionsViewModel)
                }
            }
        }
    }
}