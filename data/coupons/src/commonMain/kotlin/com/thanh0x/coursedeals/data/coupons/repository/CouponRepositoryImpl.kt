package com.thanh0x.coursedeals.data.coupons.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.core.common.platformCurrentTimeMillis
import com.thanh0x.coursedeals.data.coupons.mapper.toDomain
import com.thanh0x.coursedeals.data.coupons.mapper.toEntity
import com.thanh0x.coursedeals.data.coupons.source.LocalCouponDataSource
import com.thanh0x.coursedeals.data.coupons.source.remote.CouponService
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CouponMetadata
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.SearchRepository
import com.thanh0x.coursedeals.domain.coupons.source.RemoteCouponDataSource
import com.thanh0x.coursedeals.domain.user.source.LocalSettingsDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map

class CouponRepositoryImpl(
    private val localCouponDataSource: LocalCouponDataSource,
    private val remoteCouponDataSource: RemoteCouponDataSource,
    private val localSettingsDataSource: LocalSettingsDataSource,
    private val couponService: CouponService,
    private val searchRepository: SearchRepository,
) : CouponRepository {

    private val _metadataFlow = MutableSharedFlow<CouponMetadata>(replay = 1)

    private var lastSyncTimestamp: Long = 0

    override suspend fun getAllCoupons() = localCouponDataSource.getAllCoupons().map { it.toDomain() }

    override fun getCouponsPager(query: String?, filter: FilterData): Flow<PagingData<Coupon>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constant.ITEMS_PER_PAGE,
                enablePlaceholders = false,
            ),
        ) {
            localCouponDataSource.getFilteredCoupons(query, filter)
        }.flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun syncAllCoupons(force: Boolean) {
        val currentTime = platformCurrentTimeMillis()
        if (!force && ((currentTime - lastSyncTimestamp) < SYNC_THRESHOLD_MS)) {
            return
        }

        try {
            val response = couponService.fetchAllCoupons()
            val body = response.body()
            if ((response.isSuccessful) && (body != null)) {
                localCouponDataSource.clearALlCoupons()
                localCouponDataSource.insertCoupons(body.courses.map { it.toEntity() })
                searchRepository.rebuildKeywordCache()
                lastSyncTimestamp = currentTime
                _metadataFlow.emit(
                    CouponMetadata(
                        totalCoupon = body.totalCoupon,
                        lastFetchTime = body.lastFetchTime,
                        localFetchTime = currentTime,
                    ),
                )
            }
        } catch (@Suppress("TooGenericExceptionCaught") ignored: Exception) {
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

    override fun getShowLocalFetchTime(): Flow<Boolean> =
        localSettingsDataSource.getShowLocalFetchTime()

    override suspend fun saveShowLocalFetchTime(show: Boolean) {
        localSettingsDataSource.saveShowLocalFetchTime(show)
    }

    override suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon> {
        val currentTime = platformCurrentTimeMillis()
        val remoteResult = try {
            remoteCouponDataSource.fetchCouponDetail(courseId)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            AppResult.Error(e.message ?: "Failed to fetch coupon details")
        }

        if (remoteResult is AppResult.Success) {
            val entityToCache = remoteResult.data.toEntity().copy(cachedAt = currentTime)
            localCouponDataSource.insertCoupon(entityToCache)
            localCouponDataSource.clearExpiredCachedAt(currentTime - CACHE_EXPIRATION_MS)
            return AppResult.Success(remoteResult.data)
        }

        val cachedEntity = localCouponDataSource.getCouponById(courseId)
        if (cachedEntity != null) {
            val cachedAt = cachedEntity.cachedAt
            if (cachedAt != null && (currentTime - cachedAt <= CACHE_EXPIRATION_MS)) {
                return AppResult.Success(cachedEntity.toDomain())
            }
        }

        return remoteResult
    }

    companion object {
        private const val SYNC_INTERVAL_MINUTES = 15
        private const val SECONDS_IN_MINUTE = 60
        private const val MILLIS_IN_SECOND = 1000
        private const val SYNC_THRESHOLD_MS =
            SYNC_INTERVAL_MINUTES * SECONDS_IN_MINUTE * MILLIS_IN_SECOND.toLong()
        const val CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000L
    }
}
