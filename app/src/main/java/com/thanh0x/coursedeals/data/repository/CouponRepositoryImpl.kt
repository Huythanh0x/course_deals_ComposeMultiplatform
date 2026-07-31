package com.thanh0x.coursedeals.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.thanh0x.coursedeals.data.mapper.toDomain
import com.thanh0x.coursedeals.data.mapper.toEntity
import com.thanh0x.coursedeals.data.model.SearchHistory
import com.thanh0x.coursedeals.data.source.LocalCouponDataSource
import com.thanh0x.coursedeals.data.source.local.CouponDatabase
import com.thanh0x.coursedeals.data.source.remote.CouponService
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.domain.model.CouponMetadata
import com.thanh0x.coursedeals.domain.model.FilterData
import com.thanh0x.coursedeals.domain.repository.CouponRepository
import com.thanh0x.coursedeals.domain.source.LocalSettingsDataSource
import com.thanh0x.coursedeals.domain.source.RemoteCouponDataSource
import com.thanh0x.coursedeals.util.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CouponRepositoryImpl @Inject constructor(
    private val localCouponDataSource: LocalCouponDataSource,
    private val remoteCouponDataSource: RemoteCouponDataSource,
    private val localSettingsDataSource: LocalSettingsDataSource,
    private val couponDatabase: CouponDatabase,
    private val couponService: CouponService,
) : CouponRepository {

    private val _metadataFlow = MutableSharedFlow<CouponMetadata>(replay = 1)

    private var lastSyncTimestamp: Long = 0
    private var keywordCache: Set<String> = emptySet()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        repositoryScope.launch {
            rebuildKeywordCache()
        }
    }

    private suspend fun rebuildKeywordCache() {
        try {
            val coupons = localCouponDataSource.getAllCoupons()
            val allKeywords = mutableSetOf<String>()
            coupons.forEach { coupon ->
                coupon.title?.let { title ->
                    val words = title.lowercase()
                        .split(Regex("[^a-zA-Z0-9]"))
                        .filter { it.length >= 2 }
                    allKeywords.addAll(words)
                }
            }
            keywordCache = allKeywords
            Timber.d("KeywordCache: Rebuilt with ${keywordCache.size} unique keywords")
        } catch (e: Exception) {
            Timber.e(e, "KeywordCache: Failed to rebuild")
        }
    }

    override suspend fun getAllCoupons() = localCouponDataSource.getAllCoupons().map { it.toDomain() }

    override fun getCouponsPager(query: String?, filter: FilterData): Flow<PagingData<Coupon>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constant.ITEMS_PER_PAGE,
                enablePlaceholders = false,
            )
        ) {
            localCouponDataSource.getFilteredCoupons(query, filter)
        }.flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun syncAllCoupons(force: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (!force && ((currentTime - lastSyncTimestamp) < SYNC_THRESHOLD_MS)) {
            return
        }

        try {
            val response = couponService.fetchAllCoupons()
            val body = response.body()
            if ((response.isSuccessful) && (body != null)) {
                couponDatabase.withTransaction {
                    localCouponDataSource.clearALlCoupons()
                    localCouponDataSource.insertCoupons(body.courses.map { it.toEntity() })
                }
                rebuildKeywordCache()
                lastSyncTimestamp = currentTime
                _metadataFlow.emit(
                    CouponMetadata(
                        totalCoupon = body.totalCoupon,
                        lastFetchTime = body.lastFetchTime,
                        localFetchTime = currentTime,
                    )
                )
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Timber.e(e, "syncAllCoupons: Failed to sync deals")
        }
    }

    override suspend fun insertCoupon(coupon: Coupon) = localCouponDataSource.insertCoupon(coupon.toEntity())

    override suspend fun clearALlCoupons() = localCouponDataSource.clearALlCoupons()

    override suspend fun requestPostANewCoupon(couponUrl: String) =
        remoteCouponDataSource.requestPostANewCoupon(couponUrl)

    override suspend fun requestDeleteACoupon(couponUrl: String) =
        remoteCouponDataSource.requestDeleteACoupon(couponUrl)

    override fun getMetadataFlow(): Flow<CouponMetadata> =
        _metadataFlow.asSharedFlow()

    override fun getFilteredCountFlow(query: String?, filter: FilterData): Flow<Int> =
        localCouponDataSource.getFilteredCount(query, filter)

    override suspend fun getRecentSearches(): List<String> =
        couponDatabase.searchHistoryDao().getRecentSearches()

    override suspend fun getMatchingHistory(query: String): List<String> =
        couponDatabase.searchHistoryDao().getMatchingHistory(query.lowercase())

    override suspend fun getSearchSuggestions(query: String): List<String> {
        val lowercaseQuery = query.lowercase()
        return keywordCache.asSequence()
            .filter { it.startsWith(lowercaseQuery) }
            .sorted()
            .take(10)
            .toList()
    }

    override suspend fun saveSearchQuery(query: String) {
        if (query.isBlank()) return
        couponDatabase.searchHistoryDao().insert(
            SearchHistory(query.trim().lowercase(), System.currentTimeMillis())
        )
    }

    override fun getShowLocalFetchTime(): Flow<Boolean> =
        localSettingsDataSource.getShowLocalFetchTime()

    override suspend fun saveShowLocalFetchTime(show: Boolean) {
        localSettingsDataSource.saveShowLocalFetchTime(show)
    }

    override suspend fun fetchCouponDetail(courseId: Int) =
        remoteCouponDataSource.fetchCouponDetail(courseId)

    companion object {
        private const val SYNC_INTERVAL_MINUTES = 15
        private const val SECONDS_IN_MINUTE = 60
        private const val MILLIS_IN_SECOND = 1000
        private const val SYNC_THRESHOLD_MS =
            SYNC_INTERVAL_MINUTES * SECONDS_IN_MINUTE * MILLIS_IN_SECOND.toLong()
    }
}
