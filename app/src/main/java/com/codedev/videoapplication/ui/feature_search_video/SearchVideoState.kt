package com.codedev.videoapplication.ui.feature_search_video

import androidx.paging.PagingData
import com.codedev.videoapplication.data.models.Hit

data class SearchVideoState(
    val query: String = "",
    val lastQueryScrolled: String = "",
    val hasNotScrolledForCurrentSearch: Boolean = false,
    val pagingData: PagingData<Hit> = PagingData.empty(),
    val currentPosition: Int = 0
)
