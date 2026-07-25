package com.batdaulaptrinh.freeudemycoupons.domain.usecase.remote_coupon

import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon
import com.batdaulaptrinh.freeudemycoupons.domain.repository.CouponRepository
import com.batdaulaptrinh.freeudemycoupons.util.NetWorkResult
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
