package com.thanh0x.coursedeals.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.thanh0x.coursedeals.util.Constant
import kotlinx.parcelize.Parcelize

@Entity(Constant.COUPON_TABLE_NAME)
@Parcelize
data class Coupon(
    @PrimaryKey
    val courseId: Int,
    val author: String? = null,
    val category: String? = null,
    val contentLength: Int? = null,
    val couponCode: String? = null,
    val couponUrl: String? = null,
    val description: String? = null,
    val expiredDate: Long? = null,
    val heading: String? = null,
    val language: String? = null,
    val level: String? = null,
    val previewImage: String? = null,
    val previewVideo: String? = null,
    val rating: Double? = null,
    val reviews: Int? = null,
    val students: Int? = null,
    val subCategory: String? = null,
    val title: String? = null,
    val usesRemaining: Int? = null,
    val isNew: Boolean? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) : Parcelable
