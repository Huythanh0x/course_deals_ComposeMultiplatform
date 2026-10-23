package com.thanh0x.coursedeals.data.coupons.model

import com.google.gson.annotations.SerializedName

data class CourseReportRequest(
    @SerializedName("reason")
    val reason: String,
    @SerializedName("details")
    val details: String? = null,
)
