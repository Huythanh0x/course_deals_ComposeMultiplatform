package com.thanh0x.coursedeals.domain.usecase.remote_coupon

import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.domain.repository.CouponRepository
import com.thanh0x.coursedeals.util.NetWorkResult
import javax.inject.Inject

class FetchCouponDetailUseCase @Inject constructor(private val couponRepository: CouponRepository) {
    suspend operator fun invoke(courseId: Int): NetWorkResult<Coupon> {
        val responseData = couponRepository.fetchCouponDetail(courseId)
        return if (responseData.isSuccessful && responseData.body() is Coupon) {
            NetWorkResult.Success(responseData.body())
        } else {
            NetWorkResult.Error(responseData.message())
        }
    }
}
