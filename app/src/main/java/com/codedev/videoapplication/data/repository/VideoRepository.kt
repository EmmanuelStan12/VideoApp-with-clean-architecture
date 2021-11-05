package com.codedev.videoapplication.data.repository

import androidx.paging.PagingData
import com.codedev.videoapplication.data.models.Hit
import kotlinx.coroutines.flow.Flow

interface VideoRepository {

    suspend fun searchVideo(query: String): Flow<PagingData<Hit>>
}