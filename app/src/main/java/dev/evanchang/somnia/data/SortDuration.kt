package dev.evanchang.somnia.data

enum class SortDuration {
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