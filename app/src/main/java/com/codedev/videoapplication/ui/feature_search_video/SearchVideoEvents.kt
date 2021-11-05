package com.codedev.videoapplication.ui.feature_search_video

sealed class SearchVideoEvents {
    data class Scroll(val currentQuery: String, val currentPosition: Int = -1): SearchVideoEvents()
    data class Search(val query: String): SearchVideoEvents()
}
