package com.codedev.videoapplication.data.models

import java.io.Serializable

data class Videos(
    val large: Large,
    val medium: Medium,
    val small: Small,
    val tiny: Tiny
): Serializable