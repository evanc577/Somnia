package dev.evanchang.somnia.data

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
sealed class SubmissionSort {
    object Best : SubmissionSort()
    object Hot : SubmissionSort()
    object New : SubmissionSort()
    object Rising : SubmissionSort()
    class Top(val duration: SortDuration) : SubmissionSort()
    class Controversial(val duration: SortDuration) : SubmissionSort()

    override fun toString(): String {
        return when (this) {
            Best -> "best"
            Hot -> "hot"
            New -> "new"
            Rising -> "rising"
            is Top -> "top/${this.duration}"
            is Controversial -> "controversial/${this.duration}"
        }
    }
}

fun <T : SubmissionSort> T.durationString(): String? {
    return when (val sort: SubmissionSort = this) {
        SubmissionSort.Best -> null
        SubmissionSort.Hot -> null
        SubmissionSort.New -> null
        SubmissionSort.Rising -> null
        is SubmissionSort.Top -> sort.duration.toString()
        is SubmissionSort.Controversial -> sort.duration.toString()
    }
}