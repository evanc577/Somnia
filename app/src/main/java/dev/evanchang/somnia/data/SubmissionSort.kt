package dev.evanchang.somnia.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Keep
@Parcelize
@Serializable
sealed class SubmissionSort : Parcelable {
    @Parcelize
    object Best : SubmissionSort()
    @Parcelize
    object Hot : SubmissionSort()
    @Parcelize
    object New : SubmissionSort()
    @Parcelize
    object Rising : SubmissionSort()
    @Parcelize
    class Top(val duration: SortDuration) : SubmissionSort()
    @Parcelize
    class Controversial(val duration: SortDuration) : SubmissionSort()

    override fun toString(): String {
        return when (this) {
            Best -> "best"
            Hot -> "hot"
            New -> "new"
            Rising -> "rising"
            is Top -> "top"
            is Controversial -> "controversial"
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