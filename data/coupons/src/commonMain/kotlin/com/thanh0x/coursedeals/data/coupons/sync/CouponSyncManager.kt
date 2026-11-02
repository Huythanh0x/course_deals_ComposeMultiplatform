package com.thanh0x.coursedeals.data.coupons.sync

import com.thanh0x.coursedeals.core.common.AppResult

interface CouponSyncManager {
    suspend fun syncCoupons(force: Boolean = true): AppResult<Unit>
}
