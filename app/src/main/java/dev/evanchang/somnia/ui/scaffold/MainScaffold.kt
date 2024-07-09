package dev.evanchang.somnia.ui.scaffold

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.evanchang.somnia.ui.submissions.PreviewPostCard

@Composable
fun MainScaffold(content: @Composable () -> Unit) {
    Column {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        content()
    }
}

@Preview
@Composable
fun PreviewMainScaffold() {
    MainScaffold {
        PreviewPostCard()
    }
}