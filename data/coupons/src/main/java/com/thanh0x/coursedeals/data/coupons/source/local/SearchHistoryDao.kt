package com.thanh0x.coursedeals.data.coupons.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thanh0x.coursedeals.data.coupons.model.SearchHistory

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchHistory: SearchHistory)

    @Query("SELECT `query` FROM search_history ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentSearches(): List<String>

    @Query("SELECT `query` FROM search_history WHERE `query` LIKE :searchQuery || '%' ORDER BY timestamp DESC LIMIT 5")
    suspend fun getMatchingHistory(searchQuery: String): List<String>

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}
