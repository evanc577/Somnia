package dev.evanchang.somnia.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dev.evanchang.somnia.ui.UiConstants.CARD_PADDING
import dev.evanchang.somnia.ui.UiConstants.SPACER_SIZE

@Composable
fun BottomSheetItem(
    leadingIcon: @Composable () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    text: String,
    onClick: () -> Unit = {},
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(CARD_PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Spacer(modifier = Modifier.width(SPACER_SIZE))
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
            if (trailingIcon != null) {
                trailingIcon()
            }
        }
    }
}

@Preview
@Composable
private fun BottomSheetItemPreview() {
    val modifier = Modifier.fillMaxHeight()
    Surface {
        BottomSheetItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = "",
                    modifier = modifier,
                )
            },
            text = "Bottom sheet item",
        )
    }
}

@Preview
@Composable
private fun BottomSheetItemTrailingPreview() {
    val modifier = Modifier
        .fillMaxHeight()
        .wrapContentHeight(align = Alignment.CenterVertically)
    Surface {
        BottomSheetItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.StarOutline,
                    contentDescription = "",
                    modifier = modifier,
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "",
                    modifier = modifier,
                )
            },
            text = "Bottom sheet item",
        )
    }
}
