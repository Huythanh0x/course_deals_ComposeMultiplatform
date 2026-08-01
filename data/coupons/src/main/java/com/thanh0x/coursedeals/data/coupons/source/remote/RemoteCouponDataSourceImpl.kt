package com.thanh0x.coursedeals.data.coupons.source.remote

import com.thanh0x.coursedeals.data.coupons.mapper.toDomain
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.source.RemoteCouponDataSource
import timber.log.Timber
import javax.inject.Inject

class RemoteCouponDataSourceImpl @Inject constructor(private val couponService: CouponService) :
    RemoteCouponDataSource {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun requestPostANewCoupon(couponUrl: String): AppResult<Unit> {
        return try {
            val response = couponService.postCoupon(couponUrl)
            if (response.isSuccessful) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            // Catching generic Exception to transform any network or parsing error into AppResult
            Timber.e(e, "Error posting coupon: $couponUrl")
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun requestDeleteACoupon(couponUrl: String): AppResult<Unit> {
        return try {
            val response = couponService.deleteCoupon(couponUrl)
            if (response.isSuccessful) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting coupon: $couponUrl")
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun fetchCouponDetail(courseId: Int): AppResult<Coupon> {
        return try {
            val response = couponService.fetchCouponDetail(courseId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(body.toDomain())
            } else {
                AppResult.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching coupon detail: $courseId")
            AppResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }
}
