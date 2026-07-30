package com.thanh0x.coursedeals.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.thanh0x.coursedeals.util.Constant
import kotlinx.parcelize.Parcelize

@Entity(Constant.COUPON_TABLE_NAME)
@Parcelize
data class Coupon(
    @PrimaryKey
    @ColumnInfo(name = "course_id")
    val courseId: Int,
    
    @ColumnInfo(name = "author")
    val author: String? = null,
    
    @ColumnInfo(name = "category")
    val category: String? = null,
    
    @ColumnInfo(name = "content_length")
    val contentLength: Int? = null,
    
    @ColumnInfo(name = "coupon_code")
    val couponCode: String? = null,
    
    @ColumnInfo(name = "coupon_url")
    val couponUrl: String? = null,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "expired_date")
    val expiredDate: Long? = null,
    
    @ColumnInfo(name = "heading")
    val heading: String? = null,
    
    @ColumnInfo(name = "language")
    val language: String? = null,
    
    @ColumnInfo(name = "level")
    val level: String? = null,
    
    @ColumnInfo(name = "preview_image")
    val previewImage: String? = null,
    
    @ColumnInfo(name = "preview_video")
    val previewVideo: String? = null,
    
    @ColumnInfo(name = "rating")
    val rating: Double? = null,
    
    @ColumnInfo(name = "reviews")
    val reviews: Int? = null,
    
    @ColumnInfo(name = "students")
    val students: Int? = null,
    
    @ColumnInfo(name = "sub_category")
    val subCategory: String? = null,
    
    @ColumnInfo(name = "title")
    val title: String? = null,
    
    @ColumnInfo(name = "uses_remaining")
    val usesRemaining: Int? = null,
    
    @ColumnInfo(name = "is_new")
    val isNew: Boolean? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long? = null,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long? = null
) : Parcelable
