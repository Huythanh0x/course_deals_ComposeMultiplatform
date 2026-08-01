package com.thanh0x.coursedeals.feature.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.feature.detail.R
import com.thanh0x.coursedeals.feature.detail.databinding.ActivityCouponDetailBinding
import com.thanh0x.coursedeals.domain.coupons.Coupon
import com.thanh0x.coursedeals.core.ui.BaseActivity
import com.thanh0x.coursedeals.core.common.BundleKey
import com.thanh0x.coursedeals.core.ui.util.MapperToView
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CouponDetailActivity : BaseActivity() {
    lateinit var binding: ActivityCouponDetailBinding
    private val couponDetailViewModel: CouponDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCouponDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupFragmentResultListeners()

        val courseId = intent.extras?.getInt(BundleKey.TO_DETAIL_ACTIVITY)
        if (courseId != null) {
            setupObservers(courseId)
            couponDetailViewModel.checkIfInternetAvailable()
        } else {
            showFetchingErrorDialog("Not getting courseID")
        }
    }

    private fun setupObservers(courseId: Int) {
        collectFlow(couponDetailViewModel.uiState) { state ->
            handleUiState(state, courseId)
        }
    }

    private fun handleUiState(state: CouponDetailUiState, courseId: Int) {
        if (!state.isInternetAvailable) {
            showNoInternetDialog()
            return
        }

        if (state.isLoading) {
            showLoading(getString(CoreR.string.dialog_loading_text))
        } else {
            hideLoading()
        }

        state.error?.let {
            showFetchingErrorDialog(state.error)
        }

        state.coupon?.let {
            binding.mlCouponDetail.visibility = android.view.View.VISIBLE
            bindingCouponDataToView(it)
            setOnclickButtons(it)
        } ?: run {
            if ((!state.isLoading) && (state.error == null)) {
                couponDetailViewModel.fetchCouponDetail(courseId)
            }
        }
    }

    private fun setupFragmentResultListeners() {
        supportFragmentManager.setFragmentResultListener(
            ReportBottomSheetDialog.REQUEST_KEY,
            this,
        ) { _, _ ->
            showAlertDialog(
                getString(CoreR.string.action_report_title),
                getString(CoreR.string.report_submit_success),
            )
        }
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

    private fun bindingCouponDataToView(coupon: Coupon) {
        Timber.d("COUPON DETAIL BINDING: $coupon")
        MapperToView(this).let { mapper ->
            binding.tvRatingValue.text = mapper.mapRatingValue(coupon.rating)
            binding.rbCouponDetail.rating = mapper.mapRating(coupon.rating)
            binding.tvTimeLeft.text = mapper.mapTimeLeft(coupon.expiredTime)

            val durationText = mapper.mapContentLength(coupon.contentLength)
            Timber.d("COUPON DURATION TEXT: $durationText")
            binding.tvContentLength.text = durationText

            binding.tvCouponLeft.text = (coupon.usesRemaining ?: 0).toString()
            binding.tvCourseLevel.text = coupon.level ?: ""
            binding.tvLanguage.text = coupon.language ?: ""
            binding.tvCourseDescription.text = mapper.mapHTMLContent(coupon.description)
            binding.tvCourseHeadingTitle.text = coupon.heading ?: ""
            binding.tvNumberOfStudent.text = mapper.mapNumberOfStudent(coupon.students)
            binding.tvNumberOfReview.text = mapper.mapNumberOfReview(coupon.reviews)
            binding.tvCourseTitle.text = coupon.title ?: ""
            binding.tvCourseAuthor.text = coupon.author ?: ""
            binding.tvCourseCategory.text = coupon.category ?: ""
            Picasso.get()
                .load(coupon.previewImage)
                .error(CoreR.drawable.error_loading_image)
                .placeholder(CoreR.drawable.progress_animation)
                .into(binding.ivPreview)
        }
    }

    private fun setOnclickButtons(coupon: Coupon) {
        binding.btnEnroll.setOnClickListener {
            val enrollIntent = Intent().apply {
                setClassName(this@CouponDetailActivity.packageName, "com.thanh0x.coursedeals.feature.enroll.CouponEnrollActivity")
                putExtra(BundleKey.TO_ENROLL_ACTIVITY, coupon.couponUrl)
            }
            startActivity(enrollIntent)
        }

        binding.btnShare.setOnClickListener {
            shareLink(this, coupon.title ?: "", coupon.couponUrl ?: "")
        }
    }

    private fun showFetchingErrorDialog(cause: String) {
        Timber.e("showFetchingErrorDialog: $cause")
        showAlertDialog(
            getString(CoreR.string.coupon_detail_error_title),
            getString(CoreR.string.error_fetching_null_coupon),
            cause,
        ) {
            finish()
        }
    }

    private fun showNoInternetDialog() {
        showAlertDialog(
            getString(CoreR.string.no_internet_title),
            getString(CoreR.string.no_internet_message),
        ) {
            couponDetailViewModel.checkIfInternetAvailable()
        }
    }

    private fun showReportDialog() {
        val dialog = ReportBottomSheetDialog()
        dialog.show(supportFragmentManager, ReportBottomSheetDialog.TAG)
    }

    private fun shareLink(context: Context, title: String, url: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, url)
        context.startActivity(Intent.createChooser(intent, title))
    }
}
