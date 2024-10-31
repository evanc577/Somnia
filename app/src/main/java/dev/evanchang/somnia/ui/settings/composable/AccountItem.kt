package dev.evanchang.somnia.ui.settings.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.alorma.compose.settings.ui.SettingsMenuLink
import dev.evanchang.somnia.appSettings.AccountSettings
import dev.evanchang.somnia.ui.UiConstants.CARD_PADDING
import dev.evanchang.somnia.ui.UiConstants.DIALOG_HEADER_SPACING
import dev.evanchang.somnia.ui.UiConstants.SPACER_SIZE

@Composable
fun AccountItem(
    username: String?,
    accountSettings: AccountSettings?,
    isActiveUser: Boolean,
    onSetActiveUser: () -> Unit,
    onDeleteUser: () -> Unit,
) {
    var showAccountSettingsDialog by remember { mutableStateOf(false) }
    if (username != null && accountSettings != null && showAccountSettingsDialog) {
        AccountSettingsDialog(
            username = username,
            accountSettings = accountSettings,
            onDismissRequest = { showAccountSettingsDialog = false },
            onDeleteUser = onDeleteUser
        )
    }

    SettingsMenuLink(
        modifier = Modifier.fillMaxWidth(),
        title = {
            val usernameText = if (username == null) {
                "Anonymous"
            } else {
                "u/${username}"
            }
            Text(
                text = usernameText, style = MaterialTheme.typography.bodyLarge.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "active user icon",
                tint = if (isActiveUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Black.copy(alpha = 0f)
                }
            )
        },
        action = {
            if (accountSettings != null) {
                IconButton(onClick = { showAccountSettingsDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "account settings"
                    )
                }
            }
        },
        onClick = onSetActiveUser,
    )
}

@Composable
private fun AccountSettingsDialog(
    username: String,
    accountSettings: AccountSettings,
    onDismissRequest: () -> Unit,
    onDeleteUser: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(CARD_PADDING)) {
                Text(text = "Account details", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(DIALOG_HEADER_SPACING))

                // Username
                Text(text = "User", style = MaterialTheme.typography.labelLarge)
                SelectionContainer {
                    Text(text = username)
                }
                Spacer(modifier = Modifier.height(SPACER_SIZE))

                // Client ID
                Text(text = "Client ID", style = MaterialTheme.typography.labelLarge)
                SelectionContainer {
                    Text(text = accountSettings.clientId, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(SPACER_SIZE))

                // Redirect URI
                Text(text = "Redirect URI", style = MaterialTheme.typography.labelLarge)
                SelectionContainer {
                    Text(text = accountSettings.redirectUri, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(SPACER_SIZE))

                // Action buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (!showDeleteConfirm) {
                        TextButton(onClick = { showDeleteConfirm = true }) {
                            Text(text = "Delete account", color = MaterialTheme.colorScheme.error)
                        }
                        TextButton(onClick = onDismissRequest) {
                            Text(text = "Dismiss")
                        }
                    } else {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text(text = "Cancel")
                        }
                        TextButton(
                            onClick = {
                                onDeleteUser()
                                onDismissRequest()
                            },
                        ) {
                            Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AccountItemPreview() {
    Surface {
        AccountItem(
            username = "user",
            accountSettings = AccountSettings(
                clientId = "client_id",
                refreshToken = "refresh_token",
                redirectUri = "http://127.0.0.1"
            ),
            isActiveUser = true,
            onSetActiveUser = {},
            onDeleteUser = {},
        )
    }
}

@Preview
@Composable
private fun AccountSettingsDialogPreview() {
    AccountSettingsDialog(
        username = "username",
        accountSettings = AccountSettings(
            clientId = "client_id",
            refreshToken = "refresh_token",
            redirectUri = "http://127.0.0.1",
        ),
        onDismissRequest = {},
        onDeleteUser = {},
    )
}