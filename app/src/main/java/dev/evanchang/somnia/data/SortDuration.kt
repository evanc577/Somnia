package dev.evanchang.somnia.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class SortDuration : Parcelable {
    HOUR,
    DAY,
    WEEK,
    MONTH,
    YEAR,
    ALL,
    ;

    override fun toString(): String {
        return when (this) {
            HOUR -> "hour"
            DAY -> "day"
            WEEK -> "week"
            MONTH -> "month"
            YEAR -> "year"
            ALL -> "all"
        }
    }
}