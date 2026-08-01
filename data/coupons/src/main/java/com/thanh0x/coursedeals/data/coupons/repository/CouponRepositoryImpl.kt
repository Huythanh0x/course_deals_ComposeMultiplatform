package com.thanh0x.coursedeals.data.coupons.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.thanh0x.coursedeals.data.coupons.mapper.toDomain
import com.thanh0x.coursedeals.data.coupons.mapper.toEntity
import com.thanh0x.coursedeals.data.coupons.source.LocalCouponDataSource
import com.thanh0x.coursedeals.data.coupons.source.local.CouponDatabase
import com.thanh0x.coursedeals.data.coupons.source.remote.CouponService
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CouponMetadata
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.coupons.SearchRepository
import com.thanh0x.coursedeals.domain.user.source.LocalSettingsDataSource
import com.thanh0x.coursedeals.domain.coupons.source.RemoteCouponDataSource
import com.thanh0x.coursedeals.core.common.Constant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class CouponRepositoryImpl @Inject constructor(
    private val localCouponDataSource: LocalCouponDataSource,
    private val remoteCouponDataSource: RemoteCouponDataSource,
    private val localSettingsDataSource: LocalSettingsDataSource,
    private val couponDatabase: CouponDatabase,
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
