package com.thanh0x.coursedeals.domain.model

data class Coupon(
    val courseId: Int,
    val author: String?,
    val category: String?,
    val contentLength: Int?,
    val couponCode: String?,
    val couponUrl: String?,
    val description: String?,
    val expiredTime: Long?,
    val heading: String?,
    val language: String?,
    val level: String?,
    val previewImage: String?,
    val previewVideo: String?,
    val rating: Double?,
    val reviews: Int?,
    val students: Int?,
    val subCategory: String?,
    val title: String?,
    val usesRemaining: Int?,
    val isNew: Boolean?,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
