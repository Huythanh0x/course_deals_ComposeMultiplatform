package com.thanh0x.coursedeals.data.model

import com.google.gson.annotations.SerializedName

data class CouponDto(
    @SerializedName("courseId")
    val courseId: Int,
    @SerializedName("author")
    val author: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("contentLength")
    val contentLength: Int? = null,
    @SerializedName("couponCode")
    val couponCode: String? = null,
    @SerializedName("couponUrl")
    val couponUrl: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("expiredDate")
    val expiredDate: Double? = null,
    @SerializedName("heading")
    val heading: String? = null,
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("level")
    val level: String? = null,
    @SerializedName("previewImage")
    val previewImage: String? = null,
    @SerializedName("previewVideo")
    val previewVideo: String? = null,
    @SerializedName("rating")
    val rating: Double? = null,
    @SerializedName("reviews")
    val reviews: Int? = null,
    @SerializedName("students")
    val students: Int? = null,
    @SerializedName("subCategory")
    val subCategory: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("usesRemaining")
    val usesRemaining: Int? = null,
    @SerializedName("isNew")
    val isNew: Boolean? = null,
    @SerializedName("createdAt")
    val createdAt: List<Int>? = null,
    @SerializedName("updatedAt")
    val updatedAt: List<Int>? = null
)
