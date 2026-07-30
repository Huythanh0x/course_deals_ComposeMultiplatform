package com.thanh0x.coursedeals.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.thanh0x.coursedeals.data.mapper.toDomain
import com.thanh0x.coursedeals.data.mapper.toEntity
import com.thanh0x.coursedeals.data.source.LocalCouponDataSource
import com.thanh0x.coursedeals.data.source.local.CouponDatabase
import com.thanh0x.coursedeals.data.source.remote.CouponService
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.domain.model.CouponMetadata
import com.thanh0x.coursedeals.domain.repository.CouponRepository
import com.thanh0x.coursedeals.domain.source.LocalSettingsDataSource
import com.thanh0x.coursedeals.domain.source.RemoteCouponDataSource
import com.thanh0x.coursedeals.util.Constant
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
) : CouponRepository {

    private val _metadataFlow = MutableSharedFlow<CouponMetadata>(replay = 1)

    override suspend fun getAllCoupons() = localCouponDataSource.getAllCoupons().map { it.toDomain() }

    override fun getCouponsPager(query: String?): Flow<PagingData<Coupon>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constant.ITEMS_PER_PAGE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                if (query.isNullOrBlank()) {
                    localCouponDataSource.getPagingCoupons()
                } else {
                    localCouponDataSource.queryCouponByName(query)
                }
            }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }

    private var lastSyncTimestamp: Long = 0
    private val SYNC_THRESHOLD_MS = 15 * 60 * 1000L // 15 minutes

    override suspend fun syncAllCoupons(force: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (!force && (currentTime - lastSyncTimestamp < SYNC_THRESHOLD_MS)) {
            return
        }

        try {
            val response = couponService.fetchAllCoupons()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                couponDatabase.withTransaction {
                    localCouponDataSource.clearALlCoupons()
                    localCouponDataSource.insertCoupons(body.courses.map { it.toEntity() })
                }
                lastSyncTimestamp = currentTime
                _metadataFlow.emit(
                    CouponMetadata(
                        totalCoupon = body.totalCoupon,
                        lastFetchTime = body.lastFetchTime,
                        localFetchTime = currentTime
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "syncAllCoupons: Failed to sync deals")
        }
    }

    override suspend fun insertCoupon(coupon: Coupon) = localCouponDataSource.insertCoupon(coupon.toEntity())

    override suspend fun queryCouponByName(query: String) =
        localCouponDataSource.queryCouponByName(query).map { it.toDomain() }

    override suspend fun clearALlCoupons() = localCouponDataSource.clearALlCoupons()

    override suspend fun requestPostANewCoupon(couponUrl: String) =
        remoteCouponDataSource.requestPostANewCoupon(couponUrl)

    override suspend fun requestDeleteACoupon(couponUrl: String) =
        remoteCouponDataSource.requestDeleteACoupon(couponUrl)

    override fun getMetadataFlow(): Flow<CouponMetadata> =
        _metadataFlow.asSharedFlow()

    override fun getShowLocalFetchTime(): Flow<Boolean> =
        localSettingsDataSource.getShowLocalFetchTime()

    override suspend fun saveShowLocalFetchTime(show: Boolean) {
        localSettingsDataSource.saveShowLocalFetchTime(show)
    }

    override suspend fun fetchCouponDetail(courseId: Int) =
        remoteCouponDataSource.fetchCouponDetail(courseId)
}
