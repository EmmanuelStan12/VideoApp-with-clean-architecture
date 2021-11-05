package com.codedev.videoapplication.domain.usecases

import com.codedev.videoapplication.data.repository.VideoRepository
import javax.inject.Inject

class GetVideo @Inject constructor(
    private val repository: VideoRepository
) {

    suspend operator fun invoke() {

    }
}