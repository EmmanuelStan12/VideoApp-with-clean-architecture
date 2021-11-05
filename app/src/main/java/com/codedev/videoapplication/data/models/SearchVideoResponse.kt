package com.codedev.videoapplication.data.models

data class SearchVideoResponse(
    val hits: List<Hit>,
    val total: Int,
    val totalHits: Int
)