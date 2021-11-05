package com.codedev.videoapplication.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.codedev.videoapplication.data.models.Hit
import com.codedev.videoapplication.data.remote_api.VideoApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

const val DEFAULT_INIT_PAGE = 1

class VideoRemotePagingSource @Inject constructor(
    private val videoApi: VideoApi,
    private val query: String
) : PagingSource<Int, Hit>(){

    override fun getRefreshKey(state: PagingState<Int, Hit>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Hit> {
        val page = params.key ?: DEFAULT_INIT_PAGE
        return try {
            val response = videoApi.searchVideo(
                query = query,
                page = page,
                perPage = params.loadSize
            )
            val hits = response.body()?.hits ?: emptyList()
            LoadResult.Page(
                data = hits,
                prevKey = if(page == DEFAULT_INIT_PAGE) null else page - 1,
                nextKey = if(hits.isEmpty()) null else page + 1
            )
        }catch (e: IOException) {
            LoadResult.Error(e)
        }catch (e: HttpException) {
            LoadResult.Error(e)
        }catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}