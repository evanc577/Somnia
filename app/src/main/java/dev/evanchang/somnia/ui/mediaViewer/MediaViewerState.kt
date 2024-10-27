package dev.evanchang.somnia.ui.mediaViewer

import dev.evanchang.somnia.data.Submission

sealed class MediaViewerState {
    data object NotShowing : MediaViewerState()
    class Showing(val submission: Submission) : MediaViewerState()
}
