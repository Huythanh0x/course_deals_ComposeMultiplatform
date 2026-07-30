package com.thanh0x.coursedeals.data.mapper

import android.annotation.SuppressLint
import com.thanh0x.coursedeals.data.model.Coupon as CouponEntity
import com.thanh0x.coursedeals.data.model.CouponDto
import com.thanh0x.coursedeals.domain.model.Coupon as CouponDomain
import com.thanh0x.coursedeals.util.Constant
import java.time.LocalDateTime
import java.time.ZoneOffset
import timber.log.Timber

@SuppressLint("NewApi")
fun CouponDto.toDomain(): CouponDomain {
    return CouponDomain(
        courseId = courseId,
        author = author,
        category = category,
        contentLength = contentLength,
        couponCode = couponCode,
        couponUrl = couponUrl,
        description = description,
        expiredDate = expiredDate?.toLong(),
        heading = heading,
        language = language,
        level = level,
        previewImage = previewImage,
        previewVideo = previewVideo,
        rating = rating,
        reviews = reviews,
        students = students,
        subCategory = subCategory,
        title = title,
        usesRemaining = usesRemaining,
        isNew = isNew,
        createdAt = formatDateArrayToEpoch(createdAt),
        updatedAt = formatDateArrayToEpoch(updatedAt)
    )
}

@SuppressLint("NewApi")
fun CouponDto.toEntity(): CouponEntity {
    return CouponEntity(
        courseId = courseId,
        author = author,
        category = category,
        contentLength = contentLength,
        couponCode = couponCode,
        couponUrl = couponUrl,
        description = description,
        expiredDate = expiredDate?.toLong(),
        heading = heading,
        language = language,
        level = level,
        previewImage = previewImage,
        previewVideo = previewVideo,
        rating = rating,
        reviews = reviews,
        students = students,
        subCategory = subCategory,
        title = title,
        usesRemaining = usesRemaining,
        isNew = isNew,
        createdAt = formatDateArrayToEpoch(createdAt),
        updatedAt = formatDateArrayToEpoch(updatedAt)
    )
}

fun CouponEntity.toDomain(): CouponDomain {
    return CouponDomain(
        courseId = courseId,
        author = author,
        category = category,
        contentLength = contentLength,
        couponCode = couponCode,
        couponUrl = couponUrl,
        description = description,
        expiredDate = expiredDate,
        heading = heading,
        language = language,
        level = level,
        previewImage = previewImage,
        previewVideo = previewVideo,
        rating = rating,
        reviews = reviews,
        students = students,
        subCategory = subCategory,
        title = title,
        usesRemaining = usesRemaining,
        isNew = isNew,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CouponDomain.toEntity(): CouponEntity {
    return CouponEntity(
        courseId = courseId,
        author = author,
        category = category,
        contentLength = contentLength,
        couponCode = couponCode,
        couponUrl = couponUrl,
        description = description,
        expiredDate = expiredDate,
        heading = heading,
        language = language,
        level = level,
        previewImage = previewImage,
        previewVideo = previewVideo,
        rating = rating,
        reviews = reviews,
        students = students,
        subCategory = subCategory,
        title = title,
        usesRemaining = usesRemaining,
        isNew = isNew,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Converts a JSON date array [year, month, day, hour, minute, second] to Epoch Seconds.
 */
@SuppressLint("NewApi")
@Suppress("TooGenericExceptionCaught")
private fun formatDateArrayToEpoch(dateArray: List<Int>?): Long? {
    if (dateArray == null || dateArray.size < Constant.DATE_ARRAY_MIN_SIZE) return null
    return try {
        val year = dateArray[0]
        val month = dateArray[1]
        val day = dateArray[2]
        val hour = if (dateArray.size > 3) dateArray[3] else 0
        val minute = if (dateArray.size > 4) dateArray[4] else 0
        val second = if (dateArray.size > 5) dateArray[5] else 0
        
        LocalDateTime.of(year, month, day, hour, minute, second)
            .toEpochSecond(ZoneOffset.UTC)
    } catch (e: Exception) {
        Timber.e(e, "Error formatting date array to epoch: $dateArray")
        null
    }
}
