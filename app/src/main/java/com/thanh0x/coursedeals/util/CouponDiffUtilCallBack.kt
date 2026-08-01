package com.thanh0x.coursedeals.util

import androidx.recyclerview.widget.DiffUtil
import com.thanh0x.coursedeals.domain.coupons.Coupon

object CouponDiffUtilCallBack :
    DiffUtil.ItemCallback<Coupon>() {
    override fun areItemsTheSame(oldItem: Coupon, newItem: Coupon): Boolean =
        oldItem.courseId == newItem.courseId

    override fun areContentsTheSame(oldItem: Coupon, newItem: Coupon): Boolean =
        oldItem == newItem
}
