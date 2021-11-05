package com.codedev.videoapplication.data.models

import java.io.Serializable

data class Hit(
    val comments: Int,
    val downloads: Int,
    val duration: Int,
    val id: Int,
    val likes: Int,
    val pageURL: String,
    val picture_id: String,
    val tags: String,
    val type: String,
    val user: String,
    val userImageURL: String,
    val user_id: Int,
    val videos: Videos,
    val views: Int
): Serializable

fun Hit.transformTags(): String {
    val newTags =  tags.replace(",", "")
    val tags = newTags.split(" ")
    val builder = StringBuilder()
    for(tag in tags) {
        builder.append("#").append(tag).append("  ")
    }
    return builder.toString()
}