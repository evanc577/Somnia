package dev.evanchang.somnia.ui.settings.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsDirectory(onClick: () -> Unit, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }) {
            Text(
                text = text, fontSize = 20.sp, modifier = Modifier.padding(20.dp)
            )
        }
    }
}

@Preview
@Composable
private fun SettingsDirectoryPreview() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            SettingsDirectory(onClick = {}, text = "Directory 1")
            SettingsDirectory(onClick = {}, text = "Directory 2")
            SettingsDirectory(onClick = {}, text = "Directory 3")
        }
    }
}