package com.fbmanager.domain.repository

import com.fbmanager.domain.model.FavoriteNode
import com.fbmanager.domain.model.UserNode

interface UserRepository {
    suspend fun getUser(userId: String): UserNode?
    suspend fun getUserFavorites(userId: String, limit: Int = 50, startAfter: String? = null): Pair<List<FavoriteNode>, String?>
    suspend fun getTotalUsersCount(): Long
    suspend fun getMostRecentlyActiveUsers(limit: Int = 20, startAfterValue: Long? = null, startAfterKey: String? = null): Pair<List<UserNode>, Pair<Long, String>?>
    suspend fun getTopUsersByDownloads(limit: Int = 20, startAfterValue: Long? = null, startAfterKey: String? = null): Pair<List<UserNode>, Pair<Long, String>?>
}
