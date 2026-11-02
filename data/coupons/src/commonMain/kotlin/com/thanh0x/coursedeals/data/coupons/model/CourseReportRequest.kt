package com.thanh0x.coursedeals.data.coupons.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseReportRequest(
    @SerialName("reason")
    val reason: String,
    @SerialName("details")
    val details: String? = null,
)
