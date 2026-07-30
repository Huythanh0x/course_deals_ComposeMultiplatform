package com.thanh0x.coursedeals.data.mapper

import com.thanh0x.coursedeals.data.model.Coupon as CouponDto
import com.thanh0x.coursedeals.domain.model.Coupon as CouponDomain

fun CouponDto.toDomain(): CouponDomain {
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
        isNew = isNew
    )
}

fun CouponDomain.toDto(): CouponDto {
    return CouponDto(
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
        isNew = isNew
    )
}
