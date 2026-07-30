package com.thanh0x.coursedeals.domain.usecase.remotecoupon

import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.domain.repository.CouponRepository
import javax.inject.Inject

class FetchCouponDetailUseCase @Inject constructor(private val couponRepository: CouponRepository) {
    suspend operator fun invoke(courseId: Int): AppResult<Coupon> {
        return couponRepository.fetchCouponDetail(courseId)
    }
}
