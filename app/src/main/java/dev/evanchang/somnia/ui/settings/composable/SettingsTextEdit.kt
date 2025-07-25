package dev.evanchang.somnia.ui.settings.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.alorma.compose.settings.ui.base.internal.SettingsTileScaffold
import dev.evanchang.somnia.ui.UiConstants.CARD_PADDING
import dev.evanchang.somnia.ui.UiConstants.DIALOG_HEADER_SPACING
import dev.evanchang.somnia.ui.UiConstants.SPACER_SIZE

@Composable
fun SettingsTextEdit(
    title: String,
    subtitle: @Composable (() -> Unit)? = null,
    description: String? = null,
    defaultText: String = "",
    onConfirmRequest: (String) -> Unit
) {
    var text by remember { mutableStateOf(defaultText) }
    var openDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(openDialog) {
        if (openDialog) {
            focusRequester.requestFocus()
        }
    }

    SettingsTileScaffold(
        modifier = Modifier.clickable { openDialog = true },
        title = { Text(text = title) },
        subtitle = subtitle,
    )
    if (openDialog) {
        SettingsTextEditDialog(
            onDismissRequest = { openDialog = false },
            title = title,
            description = description,
            value = text,
            focusRequester = focusRequester,
            onValueChange = { text = it },
            onConfirmRequest = onConfirmRequest,
        )
    }
}

@Composable
private fun SettingsTextEditDialog(
    title: String,
    description: String?,
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmRequest: (String) -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(CARD_PADDING)) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                if (description != null) {
                    Spacer(modifier = Modifier.height(DIALOG_HEADER_SPACING))
                    Text(text = description)
                }
                Spacer(modifier = Modifier.height(DIALOG_HEADER_SPACING))
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        onDismissRequest()
                        onConfirmRequest(value)
                    }),
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester),
                )
                Spacer(modifier = Modifier.height(SPACER_SIZE))
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = "Cancel")
                    }
                    TextButton(onClick = {
                        onDismissRequest()
                        onConfirmRequest(value)
                    }) {
                        Text(text = "Confirm")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SettingsTextEditPreview() {
    SettingsTextEdit(
        title = "SettingsTextEdit",
        description = "This is the description for SettingsTextEdit",
        defaultText = "Initial text",
        onConfirmRequest = {})
}

@Preview
@Composable
private fun SettingsTextEditDialogPreview() {
    SettingsTextEditDialog(
        title = "Title",
        description = "Description",
        value = "value",
        focusRequester = FocusRequester(),
        onValueChange = {},
        onDismissRequest = {},
        onConfirmRequest = {},
    )
}