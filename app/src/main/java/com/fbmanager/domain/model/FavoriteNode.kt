package com.fbmanager.domain.model

data class FavoriteNode(
    val trackId: String,
    val title: String,
    val artist: String,
    val addedAt: Long? = null
)
