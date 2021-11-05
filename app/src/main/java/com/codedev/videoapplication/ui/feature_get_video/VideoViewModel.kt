package com.codedev.videoapplication.ui.feature_get_video

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codedev.videoapplication.data.models.Hit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class VideoViewModel @Inject constructor(
    application: Application
): AndroidViewModel(application) {

    private val _state = MutableStateFlow(VideoState())
    val state get() = _state

    fun execute(event: VideoEvents) {
        viewModelScope.launch {
            when(event) {
                is VideoEvents.UpdateHit -> {
                    _state.value = state.value.copy(
                        hit = event.hit
                    )
                }
                is VideoEvents.SaveVideoState -> {
                    _state.value = state.value.copy(
                        currentWindow = event.state.currentWindow,
                        playbackPosition = event.state.playbackPosition,
                        playWhenReady = event.state.playWhenReady,
                    )
                }
                is VideoEvents.UpdateOrientation -> {
                    _state.value = state.value.copy(
                        orientation = event.orientation
                    )
                }
            }
        }
    }
}

enum class UiOrientation {
    PORTRAIT, LANDSCAPE
}

sealed class VideoEvents {
    data class UpdateHit(val hit: Hit): VideoEvents()
    data class UpdateOrientation(val orientation: UiOrientation): VideoEvents()
    data class SaveVideoState(val state: VideoState) : VideoEvents()
}