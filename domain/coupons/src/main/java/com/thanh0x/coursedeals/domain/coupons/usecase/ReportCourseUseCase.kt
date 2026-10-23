package com.thanh0x.coursedeals.domain.coupons.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import javax.inject.Inject

class ReportCourseUseCase @Inject constructor(
    private val couponRepository: CouponRepository,
) {
    suspend operator fun invoke(
        courseId: Long,
        reason: String,
        details: String?,
    ): AppResult<Unit> {
        return couponRepository.reportCourse(courseId, reason, details)
    }
}
