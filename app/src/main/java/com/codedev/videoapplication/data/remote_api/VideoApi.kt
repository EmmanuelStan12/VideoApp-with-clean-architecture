package com.codedev.videoapplication.data.remote_api

import com.codedev.videoapplication.data.models.SearchVideoResponse
import com.codedev.videoapplication.data.utils.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VideoApi {

    @GET("videos")
    suspend fun searchVideo(
        @Query("key") key: String = API_KEY,
        @Query("q") query: String,
        @Query("video_type") videoType: String = "all",
        @Query("category") category: String = "science",
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20,
        @Query("safesearch") safeSearch: Boolean = true
    ): Response<SearchVideoResponse>
}