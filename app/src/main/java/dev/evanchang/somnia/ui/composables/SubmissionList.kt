package dev.evanchang.somnia.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import dev.evanchang.somnia.api.reddit.SubredditSubmissionsViewModel
import dev.evanchang.somnia.data.Submission
import kotlinx.coroutines.flow.Flow

@Composable
fun SubmissionList(submissions: Flow<PagingData<Submission>>) {
    val lazySubmissionItems: LazyPagingItems<Submission> = submissions.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceDim)
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
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(all = 16.dp)
    ) {
        Column {
            SubmissionCardHeader(submission = submission)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = submission.escapedTitle(), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            SubmissionCardPreview(submission = submission)
        }
    }
}

@Composable
private fun SubmissionCardHeader(submission: Submission) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.twotone_account_circle_24),
            contentDescription = "Subreddit Icon",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(text = buildAnnotatedString {
                append("r/")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold
                    )
                ) {
                    append(submission.subreddit)
                }
            }, style = MaterialTheme.typography.labelSmall)
            Text(text = "u/${submission.author}", style = MaterialTheme.typography.labelSmall)
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
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .align(Alignment.Center)
        )
    }
}

@Preview
@Composable
fun PreviewPostList() {
    val mainViewModel = SubredditSubmissionsViewModel()
    MaterialTheme {
        SubmissionList(submissions = mainViewModel.submissions)
    }
}
