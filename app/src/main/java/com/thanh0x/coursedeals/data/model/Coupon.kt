package com.thanh0x.coursedeals.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.thanh0x.coursedeals.util.Constant
import kotlinx.parcelize.Parcelize


import com.google.gson.annotations.SerializedName

@Entity(Constant.COUPON_TABLE_NAME)
@Parcelize
data class Coupon(
    @PrimaryKey
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
    @SerializedName("new")
    val isNew: Boolean? = null
) : Parcelable