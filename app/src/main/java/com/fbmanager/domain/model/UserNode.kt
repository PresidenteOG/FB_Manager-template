package com.fbmanager.domain.model

data class UserNode(
    val id: String,
    val lastConnected: Long? = null,
    val downloadCount: Int = 0,
    val totalPlayTime: Int = 0,
    val trackPlays: Int = 0,
    val isAdmin: Boolean = false
)
