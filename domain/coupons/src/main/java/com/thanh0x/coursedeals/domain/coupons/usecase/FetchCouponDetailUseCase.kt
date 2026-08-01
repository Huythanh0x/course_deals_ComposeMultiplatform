package com.thanh0x.coursedeals.domain.coupons.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import javax.inject.Inject

class FetchCouponDetailUseCase @Inject constructor(private val couponRepository: CouponRepository) {
    suspend operator fun invoke(courseId: Int): AppResult<Coupon> {
        return couponRepository.fetchCouponDetail(courseId)
    }
}
