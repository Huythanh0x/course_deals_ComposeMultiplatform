package com.thanh0x.coursedeals.data.coupons.mapper

import com.thanh0x.coursedeals.core.network.openapi.model.CouponDetailDTO
import com.thanh0x.coursedeals.core.network.openapi.model.CouponSummaryDTO
import com.thanh0x.coursedeals.data.coupons.model.Coupon as CouponEntity
import com.thanh0x.coursedeals.domain.coupons.Coupon as CouponDomain

fun CouponDetailDTO.toDomain(): CouponDomain {
    return CouponDomain(
        courseId = courseId,
        author = author,
        category = category,
        contentLength = contentLength,
        couponCode = couponCode,
        couponUrl = couponUrl,
        description = description,
        expiredTime = expiredTime,
        heading = heading,
        language = language,
        level = level?.value,
        previewImage = previewImage,
        previewVideo = previewVideo,
        rating = rating.toDouble(),
        reviews = reviews,
        students = students,
        subCategory = subCategory,
        title = title,
        usesRemaining = usesRemaining,
        isNew = isNew,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun CouponDetailDTO.toEntity(): CouponEntity {
    return CouponEntity(
        courseId = courseId,
        author = author,
        category = category,
        contentLength = contentLength,
        couponCode = couponCode,
        couponUrl = couponUrl,
        description = description,
        expiredTime = expiredTime,
        heading = heading,
        language = language,
        level = level?.value,
        previewImage = previewImage,
        previewVideo = previewVideo,
        rating = rating.toDouble(),
        reviews = reviews,
        students = students,
        subCategory = subCategory,
        title = title,
        usesRemaining = usesRemaining,
        isNew = isNew,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun CouponSummaryDTO.toDomain(): CouponDomain {
    return CouponDomain(
        courseId = courseId,
        author = author,
        category = category,
        contentLength = null,
        couponCode = null,
        couponUrl = null,
        description = null,
        expiredTime = expiredTime,
        heading = null,
        language = language,
        level = level?.value,
        previewImage = previewImage,
        previewVideo = null,
        rating = rating.toDouble(),
        reviews = reviews,
        students = students,
        subCategory = null,
        title = title,
        usesRemaining = null,
        isNew = isNew,
        createdAt = createdAt,
        updatedAt = null,
    )
}

fun CouponSummaryDTO.toEntity(): CouponEntity {
    return CouponEntity(
        courseId = courseId,
        author = author,
        category = category,
        contentLength = null,
        couponCode = null,
        couponUrl = null,
        description = null,
        expiredTime = expiredTime,
        heading = null,
        language = language,
        level = level?.value,
        previewImage = previewImage,
        previewVideo = null,
        rating = rating.toDouble(),
        reviews = reviews,
        students = students,
        subCategory = null,
        title = title,
        usesRemaining = null,
        isNew = isNew,
        createdAt = createdAt,
        updatedAt = null,
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
        expiredTime = expiredTime,
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
        updatedAt = updatedAt,
        cachedAt = cachedAt,
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
        expiredTime = expiredTime,
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
        updatedAt = updatedAt,
        cachedAt = cachedAt,
    )
}
