package com.thanh0x.coursedeals.data.coupons.source.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.thanh0x.coursedeals.data.coupons.model.Coupon
import com.thanh0x.coursedeals.data.coupons.model.SearchHistory
import com.thanh0x.coursedeals.data.coupons.source.LocalCouponDataSource
import com.thanh0x.coursedeals.domain.coupons.CourseLanguage
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class LocalCouponDataSourceImpl : LocalCouponDataSource {
    private val couponsState = MutableStateFlow<Map<Int, Coupon>>(emptyMap())
    private val searchHistoryState = MutableStateFlow<Map<String, SearchHistory>>(emptyMap())
    private val activePagingSources = MutableStateFlow<List<PagingSource<Int, Coupon>>>(emptyList())

    private fun invalidatePagingSources() {
        val sources = activePagingSources.getAndUpdate { emptyList() }
        sources.forEach { it.invalidate() }
    }

    override suspend fun getAllCoupons(): List<Coupon> = couponsState.value.values.toList()

    override fun getPagingCoupons(): PagingSource<Int, Coupon> =
        getFilteredCoupons(query = null, filter = FilterData())

    override fun getFilteredCoupons(query: String?, filter: FilterData): PagingSource<Int, Coupon> {
        val items = filterAndSortCoupons(couponsState.value.values.toList(), query, filter)
        return ListPagingSource(items).also { source ->
            activePagingSources.update { it + source }
        }
    }

    override fun getFilteredCount(query: String?, filter: FilterData): Flow<Int> =
        couponsState.map { map -> filterAndSortCoupons(map.values.toList(), query, filter).size }

    override suspend fun getSearchSuggestions(query: String): List<String> {
        val lower = query.lowercase()
        return couponsState.value.values
            .mapNotNull { it.title }
            .filter { it.lowercase().contains(lower) }
            .take(MAX_SEARCH_SUGGESTIONS)
    }

    override suspend fun getCouponById(courseId: Int): Coupon? = couponsState.value[courseId]

    override suspend fun clearExpiredCachedAt(expirationThreshold: Long): Int {
        var updatedCount = 0
        val current = couponsState.value.toMutableMap()
        current.forEach { (id, coupon) ->
            val cachedAt = coupon.cachedAt
            if (cachedAt != null && cachedAt < expirationThreshold) {
                current[id] = coupon.copy(cachedAt = null)
                updatedCount++
            }
        }
        if (updatedCount > 0) {
            couponsState.value = current
        }
        return updatedCount
    }

    override suspend fun insertCoupon(coupon: Coupon) {
        couponsState.update { current ->
            current + (coupon.courseId to coupon)
        }
        invalidatePagingSources()
    }

    override suspend fun insertCoupons(coupons: List<Coupon>) {
        couponsState.update { current ->
            val mutable = current.toMutableMap()
            coupons.forEach { mutable[it.courseId] = it }
            mutable
        }
        invalidatePagingSources()
    }

    override suspend fun clearALlCoupons(): Int {
        val prev = couponsState.getAndUpdate { emptyMap() }
        invalidatePagingSources()
        return prev.size
    }

    override suspend fun getRecentSearches(): List<String> {
        return searchHistoryState.value.values
            .sortedByDescending { it.timestamp }
            .take(MAX_RECENT_SEARCHES)
            .map { it.query }
    }

    override suspend fun getMatchingHistory(query: String): List<String> {
        val lower = query.lowercase()
        return searchHistoryState.value.values
            .filter { it.query.lowercase().startsWith(lower) }
            .sortedByDescending { it.timestamp }
            .take(MAX_RECENT_SEARCHES)
            .map { it.query }
    }

    override suspend fun insertSearchHistory(history: SearchHistory) {
        searchHistoryState.update { current ->
            current + (history.query to history)
        }
    }

    private fun filterAndSortCoupons(
        coupons: List<Coupon>,
        query: String?,
        filter: FilterData,
    ): List<Coupon> {
        val categoryValues = filter.categories.map { it.dbValue }.toSet()
        val lowerQuery = query?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

        val filtered = coupons.filter { coupon ->
            val matchesQuery = lowerQuery == null ||
                (coupon.title?.lowercase()?.contains(lowerQuery) == true)
            val matchesCategory = categoryValues.isEmpty() ||
                (coupon.category != null && categoryValues.contains(coupon.category))
            val matchesRating = filter.minRating <= 0.0 ||
                ((coupon.rating ?: 0.0) >= filter.minRating)
            val matchesLanguage = when (filter.language) {
                CourseLanguage.ENGLISH -> coupon.language?.lowercase() == "english"
                CourseLanguage.OTHERS -> coupon.language == null || coupon.language.lowercase() != "english"
                CourseLanguage.ALL -> true
            }
            matchesQuery && matchesCategory && matchesRating && matchesLanguage
        }

        return when (filter.sortBy) {
            SortOption.RATING -> filtered.sortedByDescending { it.rating ?: 0.0 }
            SortOption.STUDENTS -> filtered.sortedByDescending { it.students ?: 0 }
            SortOption.REVIEWS -> filtered.sortedByDescending { it.reviews ?: 0 }
            SortOption.EXPIRING_SOON -> filtered.sortedBy { it.expiredTime ?: Long.MAX_VALUE }
            SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt ?: 0L }
        }
    }

    private class ListPagingSource(
        private val items: List<Coupon>,
    ) : PagingSource<Int, Coupon>() {
        override fun getRefreshKey(state: PagingState<Int, Coupon>): Int? = null

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Coupon> {
            val fromIndex = (params.key ?: 0).coerceIn(0, items.size)
            val toIndex = (fromIndex + params.loadSize).coerceIn(fromIndex, items.size)
            val sublist = if (fromIndex < toIndex) items.subList(fromIndex, toIndex) else emptyList()
            val prevKey = if (fromIndex > 0) (fromIndex - params.loadSize).coerceAtLeast(0) else null
            val nextKey = if (toIndex < items.size) toIndex else null
            return LoadResult.Page(
                data = sublist,
                prevKey = prevKey,
                nextKey = nextKey,
            )
        }
    }

    companion object {
        private const val MAX_SEARCH_SUGGESTIONS = 10
        private const val MAX_RECENT_SEARCHES = 5
    }
}
