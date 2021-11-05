package com.codedev.videoapplication.ui.feature_get_video

import com.codedev.videoapplication.data.models.Hit

data class VideoState(
    val orientation: UiOrientation = UiOrientation.PORTRAIT,
    val hit: Hit? = null,
    val playWhenReady: Boolean = true,
    val currentWindow: Int = 0,
    val playbackPosition: Long = 0L
)
