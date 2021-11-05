package com.codedev.videoapplication.domain.usecases

import com.codedev.videoapplication.data.repository.VideoRepository
import javax.inject.Inject

class SearchVideo @Inject constructor(
    private val repository: VideoRepository
) {

    suspend operator fun invoke(query: String) = repository.searchVideo(query)
}