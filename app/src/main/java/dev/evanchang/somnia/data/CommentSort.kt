package dev.evanchang.somnia.data

enum class CommentSort {
    BEST,
    TOP,
    NEW,
    CONTROVERSIAL,
    OLD,
    RANDOM,
    QA,
    LIVE,
    ;

    override fun toString(): String {
        return when (this) {
            BEST -> "best"
            TOP -> "top"
            NEW -> "new"
            CONTROVERSIAL -> "controversial"
            OLD -> "old"
            RANDOM -> "random"
            QA -> "qa"
            LIVE -> "live"
        }
    }
}