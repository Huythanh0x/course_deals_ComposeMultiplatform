package com.thanh0x.coursedeals.domain.coupons.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.coupons.CouponRepository

class ReportCourseUseCase(
    private val repository: CouponRepository,
) {
    suspend operator fun invoke(
        courseId: Int,
        reason: String,
        details: String? = null,
    ): AppResult<Unit> = repository.reportCourse(courseId, reason, details)
}
