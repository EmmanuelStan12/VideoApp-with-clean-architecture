package com.codedev.videoapplication.data.models

import java.io.Serializable

data class Medium(
    val height: Int,
    val size: Int,
    val url: String,
    val width: Int
): Serializable