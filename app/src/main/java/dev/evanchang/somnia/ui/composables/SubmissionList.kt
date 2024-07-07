package dev.evanchang.somnia.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dev.evanchang.somnia.R
import dev.evanchang.somnia.data.PreviewImage
import dev.evanchang.somnia.data.PreviewImages
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionPreview
import dev.evanchang.somnia.ui.theme.SomniaTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun SubmissionList(
    submissions: Flow<PagingData<Submission>>,
) {
    val lazySubmissionItems: LazyPagingItems<Submission> = submissions.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val scrollState = rememberScrollState()

    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .scrollable(scrollState, Orientation.Vertical), state = listState
    ) {
        items(count = lazySubmissionItems.itemCount,
            key = { index -> lazySubmissionItems[index]!!.id }) { index ->
            val submission = lazySubmissionItems[index]
            SubmissionCard(submission = submission!!)
        }
    }
}

@Composable
private fun SubmissionCard(submission: Submission) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ), onClick = {}, modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            SubmissionCardHeader(submission = submission)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = submission.escapedTitle(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            SubmissionCardPreview(submission = submission)
            Spacer(modifier = Modifier.height(8.dp))
            SubmissionCardFooter(submission = submission)
        }
    }
}

@Composable
private fun SubmissionCardHeader(submission: Submission) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(text = buildAnnotatedString {
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
            }, style = MaterialTheme.typography.labelMedium)
            Text(
                text = "u/${submission.author}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
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
    Row {
        ScoreButton(submission = submission)
        Spacer(modifier = Modifier.width(8.dp))
        CommentsButton(submission = submission)
    }
}

@Composable
private fun ScoreButton(submission: Submission) {
    Card(onClick = {}) {
        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            Row {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.outline_keyboard_arrow_up_24),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = submission.score.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun CommentsButton(submission: Submission) {
    Card(onClick = {}) {
        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            Row {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.outline_comment_24),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = submission.numComments.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }


        }
    }
}

@Composable
private fun SubmissionCardPreview(submission: Submission) {
    val previewImage = if (submission.preview != null) {
        submission.preview.images[0].source
    } else if (submission.mediaMetadata != null) {
        submission.mediaMetadata.values.first().source
    } else {
        return
    }

    val imageRequest =
        ImageRequest.Builder(LocalContext.current).data(previewImage.escapedUrl()).crossfade(true)
            .build()

    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = "Submission image",
        contentScale = ContentScale.FillWidth,
        loading = { PreviewLoading(width = previewImage.width, height = previewImage.height) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(16.dp)
            )
    )
}

@Composable
private fun PreviewLoading(width: Int, height: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(width.toFloat() / height)
            .background(color = MaterialTheme.colorScheme.surfaceDim)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .align(Alignment.Center)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewPostCard() {
    val submission = Submission(
        id = "",
        author = "author",
        subreddit = "subreddit",
        title = "Post title",
        postHint = null,
        url = "",
        preview = SubmissionPreview(
            listOf(
                PreviewImages(
                    source = PreviewImage(
                        url = "https://i.imgur.com/c10Qvha.jpg", 1800, 1200
                    )
                )
            )
        ),
        mediaMetadata = null,
        score = 10,
        numComments = 20,
        created = 1700000000,
    )

    SomniaTheme {
        SubmissionCard(submission = submission)
    }
}
