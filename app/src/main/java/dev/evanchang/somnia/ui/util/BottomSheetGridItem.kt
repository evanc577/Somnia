package dev.evanchang.somnia.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheetGridItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.clickable { onClick() }, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                imageVector = icon, contentDescription = label, modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Preview
@Composable
private fun BottomSheetGridItemPreview() {
    Row {
        Surface {
            BottomSheetGridItem(icon = Icons.Default.Settings, label = "Settings", onClick = {})
        }
        Surface {
            BottomSheetGridItem(
                icon = Icons.Default.Settings,
                label = "This is a long name",
                onClick = {})
        }
    }
}