package com.thanh0x.coursedeals.domain.repository

interface SearchRepository {
    suspend fun getRecentSearches(): List<String>
    suspend fun getMatchingHistory(query: String): List<String>
    suspend fun getSearchSuggestions(query: String): List<String>
    suspend fun saveSearchQuery(query: String)
    suspend fun rebuildKeywordCache()
}
