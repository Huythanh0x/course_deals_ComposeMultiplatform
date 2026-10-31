package com.thanh0x.coursedeals.data.coupons.sync

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.coupons.CouponRepository

class CouponSyncManagerImpl(
    private val couponRepository: CouponRepository,
) : CouponSyncManager {

    override suspend fun syncCoupons(force: Boolean): AppResult<Unit> {
        return try {
            couponRepository.syncAllCoupons(force = force)
            AppResult.Success(Unit)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            AppResult.Error(e.message ?: "Failed to sync coupons")
        }
    }
}
