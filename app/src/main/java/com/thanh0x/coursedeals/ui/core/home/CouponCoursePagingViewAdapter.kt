package com.thanh0x.coursedeals.ui.core.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thanh0x.coursedeals.databinding.CouponCourseItemBinding
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.util.CouponDiffUtilCallBack

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
