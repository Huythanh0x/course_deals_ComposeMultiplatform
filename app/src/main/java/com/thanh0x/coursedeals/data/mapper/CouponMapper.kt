package com.thanh0x.coursedeals.data.mapper

import com.thanh0x.coursedeals.data.model.Coupon as CouponEntity
import com.thanh0x.coursedeals.data.model.CouponDto
import com.thanh0x.coursedeals.domain.coupons.Coupon as CouponDomain

fun CouponDto.toDomain(): CouponDomain {
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
    )
}

fun CouponDto.toEntity(): CouponEntity {
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
    )
}
