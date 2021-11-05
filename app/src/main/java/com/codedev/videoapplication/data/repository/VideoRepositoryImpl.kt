package com.codedev.videoapplication.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.codedev.videoapplication.data.models.Hit
import com.codedev.videoapplication.data.remote_api.VideoApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    val videoApi: VideoApi
): VideoRepository {

    override suspend fun searchVideo(query: String): Flow<PagingData<Hit>> {
        Log.d("TAG", "searchVideo: ")
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = 100,
                enablePlaceholders = false,
                initialLoadSize = 30
            ),
            pagingSourceFactory = { VideoRemotePagingSource(videoApi, query) }
        ).flow
    }
}