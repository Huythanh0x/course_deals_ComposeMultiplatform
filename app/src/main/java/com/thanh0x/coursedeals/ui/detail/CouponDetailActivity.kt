package com.thanh0x.coursedeals.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.databinding.ActivityCouponDetailBinding
import com.thanh0x.coursedeals.domain.model.AppResult
import com.thanh0x.coursedeals.domain.model.Coupon
import com.thanh0x.coursedeals.ui.base.BaseActivity
import com.thanh0x.coursedeals.ui.enroll.CouponEnrollActivity
import com.thanh0x.coursedeals.util.BundleKey
import com.thanh0x.coursedeals.util.MapperToView
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CouponDetailActivity : BaseActivity() {
    lateinit var binding: ActivityCouponDetailBinding
    private val couponDetailViewModel: CouponDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCouponDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        val courseId = intent.extras?.getInt(BundleKey.TO_DETAIL_ACTIVITY)
        showLoading(getString(R.string.dialog_loading_text))
        if (courseId != null) {
            couponDetailViewModel.isInternetAvailable.observe(this) { isInternetAvailable ->
                if (isInternetAvailable) {
                    handleIfInternetIsAvailable(courseId)
                } else {
                    showNoInternetDialog()
                }
            }
            couponDetailViewModel.checkIfInternetAvailable()
        } else {
            showFetchingErrorDialog()
        }
    }

    private fun handleIfInternetIsAvailable(courseId: Int) {
        couponDetailViewModel.couponDetail.observe(this) { result ->
            handleResult(result)
        }
        couponDetailViewModel.fetchCouponDetail(courseId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_report -> {
                    showReportDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun handleResult(result: AppResult<Coupon>) {
        when (result) {
            is AppResult.Loading -> showLoading(getString(R.string.dialog_loading_text))
            is AppResult.Error -> {
                hideLoading()
                showFetchingErrorDialog()
            }
            is AppResult.Success -> {
                hideLoading()
                binding.mlCouponDetail.visibility = android.view.View.VISIBLE
                bindingCouponDataToView(result.data)
                setOnclickButtons(result.data)
            }
        }
    }

    private fun bindingCouponDataToView(coupon: Coupon) {
        Log.d("COUPON DETAIL ", coupon.couponUrl ?: "")
        MapperToView(applicationContext).let {
            binding.rbCouponDetail.rating = it.mapRating(coupon.rating)
            binding.tvTimeLeft.text = it.mapTimeLeft(coupon.expiredDate)
            binding.tvContentLength.text = it.mapContentLength(coupon.contentLength)
            binding.tvCouponLeft.text = (coupon.usesRemaining ?: 0).toString()
            binding.tvCourseLevel.text = coupon.level ?: ""
            binding.tvLanguage.text = coupon.language ?: ""
            binding.tvCourseDescription.text = it.mapHTMLContent(coupon.description)
            binding.tvCourseHeadingTitle.text = coupon.heading ?: ""
            binding.tvNumberOfStudent.text = it.mapNumberOfStudent(coupon.students)
            binding.tvNumberOfReview.text = it.mapNumberOfReview(coupon.reviews)
            binding.tvCourseTitle.text = coupon.title ?: ""
            binding.tvCourseAuthor.text = coupon.author ?: ""
            binding.tvCourseCategory.text = coupon.category ?: ""
            Picasso.get()
                .load(coupon.previewImage)
                .error(R.drawable.error_loading_image)
                .placeholder(R.drawable.progress_animation)
                .into(binding.ivPreview)
        }
    }

    private fun setOnclickButtons(coupon: Coupon) {
        binding.btnEnroll.setOnClickListener {
            val detailIntent = Intent(this, CouponEnrollActivity::class.java)
            detailIntent.putExtra(BundleKey.TO_ENROLL_ACTIVITY, coupon.couponUrl)
            startActivity(detailIntent)
        }

        binding.btnShare.setOnClickListener {
            shareLink(this, coupon.title ?: "", coupon.couponUrl ?: "")
        }
    }

    private fun showFetchingErrorDialog() {
        showAlertDialog(
            getString(R.string.coupon_detail_error_title),
            getString(R.string.error_fetching_null_coupon)
        ) {
            finish()
        }
    }

    private fun showNoInternetDialog() {
        showAlertDialog(
            getString(R.string.no_internet_title),
            getString(R.string.no_internet_message)
        ) {
            couponDetailViewModel.checkIfInternetAvailable()
        }
    }

    private fun showReportDialog() {
        val dialog = ReportBottomSheetDialog { reason, otherDetails ->
            showAlertDialog(
                getString(R.string.action_report_title),
                getString(R.string.report_submit_success)
            )
        }
        dialog.show(supportFragmentManager, ReportBottomSheetDialog.TAG)
    }

    private fun shareLink(context: Context, title: String, url: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, url)
        context.startActivity(Intent.createChooser(intent, title))
    }
}
