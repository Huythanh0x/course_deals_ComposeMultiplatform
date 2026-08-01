package com.thanh0x.coursedeals.data.repository

import com.thanh0x.coursedeals.data.model.SearchHistory
import com.thanh0x.coursedeals.data.source.LocalCouponDataSource
import com.thanh0x.coursedeals.data.source.local.CouponDatabase
import com.thanh0x.coursedeals.domain.coupons.SearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val localCouponDataSource: LocalCouponDataSource,
    private val couponDatabase: CouponDatabase,
) : SearchRepository {

    private var keywordCache: Set<String> = emptySet()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        repositoryScope.launch {
            rebuildKeywordCache()
        }
    }

    override suspend fun rebuildKeywordCache() {
        try {
            val coupons = localCouponDataSource.getAllCoupons()
            val allKeywords = mutableSetOf<String>()
            coupons.forEach { coupon ->
                coupon.title?.let { title ->
                    val words = title.lowercase()
                        .split(Regex("[^a-zA-Z0-9]"))
                        .filter { it.length >= MIN_KEYWORD_LENGTH }
                    allKeywords.addAll(words)
                }
            }
            keywordCache = allKeywords
            Timber.d("KeywordCache: Rebuilt with ${keywordCache.size} unique keywords")
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Timber.e(e, "KeywordCache: Failed to rebuild")
        }
    }

    override suspend fun getRecentSearches(): List<String> =
        couponDatabase.searchHistoryDao().getRecentSearches()

    override suspend fun getMatchingHistory(query: String): List<String> =
        couponDatabase.searchHistoryDao().getMatchingHistory(query.lowercase())

    override suspend fun getSearchSuggestions(query: String): List<String> {
        val lowercaseQuery = query.lowercase()
        return keywordCache.asSequence()
            .filter { it.startsWith(lowercaseQuery) }
            .sorted()
            .take(MAX_SUGGESTIONS)
            .toList()
    }

    override suspend fun saveSearchQuery(query: String) {
        if (query.isBlank()) return
        couponDatabase.searchHistoryDao().insert(
            SearchHistory(
                query.trim().lowercase(),
                System.currentTimeMillis(),
            ),
        )
    }

    companion object {
        private const val MIN_KEYWORD_LENGTH = 2
        private const val MAX_SUGGESTIONS = 10
    }
}
