package dev.evanchang.somnia.ui.util

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.evanchang.somnia.data.PreviewImage
import dev.evanchang.somnia.data.PreviewImages
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionPreview
import dev.evanchang.somnia.navigation.LocalNavigation
import dev.evanchang.somnia.navigation.Nav
import dev.evanchang.somnia.ui.UiConstants.BODY_TEXT_PADDING
import dev.evanchang.somnia.ui.UiConstants.CARD_PADDING
import dev.evanchang.somnia.ui.UiConstants.ROUNDED_CORNER_RADIUS
import dev.evanchang.somnia.ui.UiConstants.SPACER_SIZE
import dev.evanchang.somnia.ui.theme.SomniaTheme
import eu.wewox.textflow.material3.TextFlow
import eu.wewox.textflow.material3.TextFlowObstacleAlignment
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

enum class SubmissionCardMode {
    PREVIEW_FULL, DETAILS,
}

@Composable
fun SubmissionCard(
    submission: Submission,
    mode: SubmissionCardMode,
) {
    val nav = LocalNavigation.current
    val media = remember { submission.media() }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS),
        modifier =
            Modifier.clickable {
                nav.onNavigate(
                    Nav.Submission(
                        initialSubmission = submission,
                        submissionId = submission.id,
                    )
                )
            },

        ) {
        Column(modifier = Modifier.padding(all = CARD_PADDING)) {
            SubmissionCardHeader(
                submission = submission,
                onClickSubreddit = { nav.onNavigate(Nav.Subreddit(submission.subreddit)) },
            )
            Spacer(modifier = Modifier.height(SPACER_SIZE))
            if (mode == SubmissionCardMode.PREVIEW_FULL && media != null) {
                Text(
                    text = submission.escapedTitle(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(SPACER_SIZE))
                SubmissionCardPreviewImage(
                    submission = submission,
                    compact = false,
                )
            } else {
                TextFlow(
                    text = submission.escapedTitle(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    obstacleAlignment = TextFlowObstacleAlignment.TopEnd,
                ) {
                    Box(modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                        SubmissionCardPreviewImage(
                            submission = submission,
                            compact = true,
                        )
                    }
                }
            }
            if (submission.selftext.isNotEmpty()) {
                Spacer(modifier = Modifier.height(SPACER_SIZE))
                Card(
                    colors = CardDefaults.cardColors()
                        .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS),
                ) {
                    when (mode) {
                        SubmissionCardMode.PREVIEW_FULL -> SomniaMarkdown(
                            content = submission.selftext,
                            isPreview = true,
                            modifier = Modifier
                                .padding(BODY_TEXT_PADDING)
                                .fillMaxWidth(),
                        )

                        SubmissionCardMode.DETAILS -> SomniaMarkdown(
                            content = submission.selftext,
                            isPreview = false,
                            modifier = Modifier
                                .padding(BODY_TEXT_PADDING)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(SPACER_SIZE))
            SubmissionCardFooter(submission = submission)
        }
    }
}

@Composable
private fun SubmissionCardHeader(submission: Submission, onClickSubreddit: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)
                    ) {
                        append("r/")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(submission.subreddit)
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable {
                    onClickSubreddit(submission.subreddit)
                },
            )
            Spacer(modifier = Modifier.height(SPACER_SIZE))
            Text(
                text = "u/${submission.author}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.weight(1.0f))
        ElapsedTime(submission = submission, modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
private fun ElapsedTime(submission: Submission, modifier: Modifier = Modifier) {
    Text(
        text = submission.elapsedTimeString(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun SubmissionCardFooter(submission: Submission) {
    Row(
        horizontalArrangement = Arrangement.End, modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        CommentsButton(submission = submission)
        Spacer(modifier = Modifier.width(SPACER_SIZE))
        ScoreButton(submission = submission)
    }
}

@Composable
private fun ScoreButton(submission: Submission) {
    val context = LocalContext.current
    Card(
        onClick = {
            // TODO: implement voting
            Toast.makeText(context, "TODO: implement voting", Toast.LENGTH_SHORT).show()
        },
        shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowUp,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(SPACER_SIZE))
            Text(
                text = submission.score.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
            )
            Spacer(modifier = Modifier.width(SPACER_SIZE))
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CommentsButton(submission: Submission) {
    Card(shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ModeComment,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(SPACER_SIZE))
            Text(
                text = submission.numComments.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SubmissionCardPreviewPreview() {
    val submission = createFakeSubmission()

    SomniaTheme {
        LazyColumn {
            for (i in 1..3) {
                item {
                    SubmissionCard(
                        submission = submission,
                        mode = SubmissionCardMode.PREVIEW_FULL,
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SubmissionCardDetailsPreview() {
    SomniaTheme {
        SubmissionCard(
            submission = createFakeSubmission(),
            mode = SubmissionCardMode.DETAILS,
        )
    }
}

@Suppress("SpellCheckingInspection")
private fun createFakeSubmission(): Submission {
    val selftext = """
        # Mattis facilisi venenatis rhoncus; tellus nibh nostra mattis ornare.
        
        Amet sem habitant ac lobortis eleifend laoreet.
        Eleifend vel risus cubilia id auctor cras.
        Pretium vehicula class elementum duis varius arcu neque vivamus cubilia.
        Varius tristique dui sapien ipsum primis aptent maximus accumsan.
        Facilisis fermentum taciti pulvinar eleifend sem dis cras.
        Urna tempor at dignissim ridiculus dolor sed iaculis auctor.
        Cras nec penatibus a augue curabitur inceptos non.
    """.trimIndent()
    return Submission(
        name = "",
        id = "",
        author = "author",
        subreddit = "subreddit",
        title = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
        """.trimIndent(),
        selftext = selftext,
        postHint = null,
        isSelf = false,
        isGallery = null,
        url = "",
        domain = "",
        preview = SubmissionPreview(
            persistentListOf(
                PreviewImages(
                    source = PreviewImage(
                        url = "https://i.imgur.com/c10Qvha.jpg", 1800, 1200
                    ),
                    resolutions = listOf<PreviewImage>().toImmutableList(),
                )
            )
        ),
        mediaMetadata = null,
        media = null,
        score = 10,
        numComments = 20,
        created = 1700000000f,
        galleryData = null,
        permalink = "",
    )
}