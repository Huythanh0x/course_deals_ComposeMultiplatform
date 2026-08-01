package com.thanh0x.coursedeals.feature.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thanh0x.coursedeals.feature.home.databinding.CouponCourseItemBinding
import com.thanh0x.coursedeals.domain.coupons.Coupon

class CouponCoursePagingViewAdapter(private val clickListener: (Coupon) -> Unit) :
    PagingDataAdapter<Coupon, CouponCoursePagingViewAdapter.CouponCourseViewHolder>(
        CouponDiffUtilCallBack
    ) {
    class CouponCourseViewHolder(private val binding: CouponCourseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(coupon: Coupon?, clickListener: (Coupon) -> Unit) {
            coupon?.let {
                binding.coupon = coupon
                binding.root.setOnClickListener {
                    clickListener(coupon)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): CouponCourseViewHolder {
        val couponItemBinding =
            CouponCourseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CouponCourseViewHolder(couponItemBinding)
    }

    override fun onBindViewHolder(
        holder: CouponCourseViewHolder, position: Int
    ) {
        holder.bind(getItem(position), clickListener)
    }
}
