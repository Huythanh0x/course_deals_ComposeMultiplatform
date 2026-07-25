package com.batdaulaptrinh.freeudemycoupons.util

import androidx.recyclerview.widget.DiffUtil
import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon

object CouponDiffUtilCallBack :
    DiffUtil.ItemCallback<Coupon>() {
    override fun areItemsTheSame(oldItem: Coupon, newItem: Coupon): Boolean =
        oldItem.courseId == newItem.courseId

    override fun areContentsTheSame(oldItem: Coupon, newItem: Coupon): Boolean =
        oldItem.courseId == newItem.courseId
}