package com.thanh0x.coursedeals.data.coupons.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouponDto(
    @SerialName("courseId")
    val courseId: Int,
    @SerialName("author")
    val author: String? = null,
    @SerialName("category")
    val category: String? = null,
    @SerialName("contentLength")
    val contentLength: Int? = null,
    @SerialName("couponCode")
    val couponCode: String? = null,
    @SerialName("couponUrl")
    val couponUrl: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("expiredTime")
    val expiredTime: Long? = null,
    @SerialName("heading")
    val heading: String? = null,
    @SerialName("language")
    val language: String? = null,
    @SerialName("level")
    val level: String? = null,
    @SerialName("previewImage")
    val previewImage: String? = null,
    @SerialName("previewVideo")
    val previewVideo: String? = null,
    @SerialName("rating")
    val rating: Double? = null,
    @SerialName("reviews")
    val reviews: Int? = null,
    @SerialName("students")
    val students: Int? = null,
    @SerialName("subCategory")
    val subCategory: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("usesRemaining")
    val usesRemaining: Int? = null,
    @SerialName("isNew")
    val isNew: Boolean? = null,
    @SerialName("createdAt")
    val createdAt: Long? = null,
    @SerialName("updatedAt")
    val updatedAt: Long? = null,
)
