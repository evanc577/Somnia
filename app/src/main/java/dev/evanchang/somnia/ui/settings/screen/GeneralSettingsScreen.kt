package dev.evanchang.somnia.ui.settings.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.compose.settings.ui.SettingsMenuLink
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold

@Composable
fun GeneralSettingsScreen() {
    var showSubmissionSortDialog by remember { mutableStateOf(false) }

    SettingsScaffold(title = "General Settings") {
        SettingsMenuLink(
            title = { Text(text = "Default submissions sort") },
            onClick = { showSubmissionSortDialog = true },
        )
    }
}

@Preview
@Composable
private fun GeneralSettingsPreview() {
    GeneralSettingsScreen()
}