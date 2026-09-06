package com.fbmanager.data.firebase

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fbmanager.domain.model.FavoriteNode
import com.fbmanager.domain.model.UserNode
import com.fbmanager.domain.repository.UserRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserRepository {

    private val database = FirebaseDatabase.getInstance()

    private val prefs by lazy {
        val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context, "fbm_user_cache", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun getUser(userId: String): UserNode? {
        return try {
            val snapshot = database.getReference("devices").child(userId).get().await()
            if (snapshot.exists()) {
                mapToUserNode(snapshot)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Error fetching user", e)
            null
        }
    }

    override suspend fun getUserFavorites(
        userId: String,
        limit: Int,
        startAfter: String?
    ): Pair<List<FavoriteNode>, String?> {
        return try {
            var query = database.getReference("devices").child(userId).child("library").child("favorites")
                .orderByKey()

            if (startAfter != null) {
                // In RTDB, to go backwards (most recent favorites), we could startAt, but Firebase keys might not be sortable by time if they are trackIds.
                // Assuming favorites are a list/map, let's just get them and sort in memory if the list isn't huge.
                // If it is large, proper pagination needs Firebase Push IDs.
                query = query.startAfter(startAfter)
            }

            val snapshot = query.limitToFirst(limit).get().await()
            val favorites = snapshot.children.mapNotNull { doc ->
                FavoriteNode(
                    trackId = doc.child("id").getValue(String::class.java) ?: doc.key ?: "",
                    title = doc.child("title").getValue(String::class.java) ?: "",
                    artist = doc.child("artist").getValue(String::class.java) ?: "",
                    addedAt = doc.child("addedAt").getValue(Long::class.java) // May need conversion if stored differently
                )
            }
            Pair(favorites, favorites.lastOrNull()?.trackId)
        } catch (e: Exception) {
            Log.e("UserRepo", "Error fetching favorites", e)
            Pair(emptyList(), null)
        }
    }

    override suspend fun getTotalUsersCount(): Long {
        return try {
            val cacheKey = "total_users_count"
            val lastUpdate = prefs.getLong("${cacheKey}_time", 0)
            val now = System.currentTimeMillis()
            
            // Cache for 1 hour to prevent huge downloads
            if (now - lastUpdate < 3600_000L) {
                val cachedCount = prefs.getLong(cacheKey, -1)
                if (cachedCount != -1L) return cachedCount
            }

            // Realtime database does not have a server-side count like Firestore.
            // We have to download the list of devices (shallow if possible, but get() downloads data).
            // This is a known limitation. We'll grab it, count it, and cache it.
            val snapshot = database.getReference("devices").get().await()
            val count = snapshot.childrenCount
            
            prefs.edit()
                .putLong(cacheKey, count)
                .putLong("${cacheKey}_time", now)
                .apply()
                
            count
        } catch (e: Exception) {
            Log.e("UserRepo", "Error fetching total users count", e)
            prefs.getLong("total_users_count", 0L)
        }
    }

    override suspend fun getMostRecentlyActiveUsers(
        limit: Int,
        startAfterValue: Long?,
        startAfterKey: String?
    ): Pair<List<UserNode>, Pair<Long, String>?> {
        return fetchUsersSortedBy("lastConnected", limit, startAfterValue, startAfterKey)
    }

    override suspend fun getTopUsersByDownloads(
        limit: Int,
        startAfterValue: Long?,
        startAfterKey: String?
    ): Pair<List<UserNode>, Pair<Long, String>?> {
        return fetchUsersSortedBy("stats/totalListenedMs", limit, startAfterValue, startAfterKey)
    }

    private suspend fun fetchUsersSortedBy(
        childPath: String,
        limit: Int,
        startAfterValue: Long?,
        startAfterKey: String?
    ): Pair<List<UserNode>, Pair<Long, String>?> {
        return try {
            var query = database.getReference("devices").orderByChild(childPath)

            if (startAfterValue != null && startAfterKey != null) {
                // To paginate descending, we use endBefore (since default is ascending)
                query = query.endBefore(startAfterValue.toDouble(), startAfterKey)
            }

            val snapshot = query.limitToLast(limit).get().await()
            val users = snapshot.children.mapNotNull { mapToUserNode(it) }.reversed() // Reverse to get descending order
            
            val lastUser = users.lastOrNull()
            val nextCursor = lastUser?.let { 
                val valToUse = if (childPath == "lastConnected") it.lastConnected else it.totalPlayTime.toLong()
                Pair(valToUse ?: 0L, it.id)
            }
            Pair(users, nextCursor)
        } catch (e: Exception) {
            Log.e("UserRepo", "Error fetching users by $childPath", e)
            Pair(emptyList(), null)
        }
    }

    private fun mapToUserNode(doc: DataSnapshot): UserNode {
        val stats = doc.child("stats")
        return UserNode(
            id = doc.key ?: "",
            lastConnected = doc.child("lastConnected").getValue(Long::class.java),
            downloadCount = doc.child("library").child("downloads").childrenCount.toInt(),
            totalPlayTime = stats.child("totalListenedMs").getValue(Long::class.java)?.toInt() ?: 0,
            trackPlays = stats.child("totalTracks").getValue(Int::class.java) ?: 0,
            isAdmin = doc.child("isAdmin").getValue(Boolean::class.java) ?: false
        )
    }
}
