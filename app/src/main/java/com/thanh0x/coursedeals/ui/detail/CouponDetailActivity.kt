package com.thanh0x.coursedeals.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.databinding.ActivityCouponDetailBinding
import com.thanh0x.coursedeals.ui.custom_view.LoadingDialog
import com.thanh0x.coursedeals.ui.enroll.CouponEnrollActivity
import com.thanh0x.coursedeals.util.BundleKey
import com.thanh0x.coursedeals.util.MapperToView
import com.thanh0x.coursedeals.util.NetWorkResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CouponDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityCouponDetailBinding
    private val couponDetailViewModel: CouponDetailViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_coupon_detail)
        setupToolbar()
        val courseId = intent.extras?.getInt(BundleKey.TO_DETAIL_ACTIVITY)
        showLoadingProgressbar()
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
        couponDetailViewModel.couponDetail.observe(this) { networkResult ->
            handleNetworkResult(networkResult)
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
                    Toast.makeText(this, "Report feature coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun handleNetworkResult(networkResult: NetWorkResult<Coupon>) {
        when (networkResult) {
            is NetWorkResult.Loading -> showLoadingProgressbar()
            is NetWorkResult.Error -> {
                hideLoadingProgressbar()
                showFetchingErrorDialog()
            }
            is NetWorkResult.Success -> {
                hideLoadingProgressbar()
                showMainContentView()
                if (networkResult.data != null) {
                    bindingCouponDataToView(networkResult.data)
                    setOnclickButtons(networkResult.data)
                } else {
                    showFetchingErrorDialog()
                }
            }
        }
    }

    private fun bindingCouponDataToView(coupon: Coupon) {
        Log.d("COUPON DETAIL ", coupon.couponUrl)
        MapperToView(applicationContext).let {
            binding.rbCouponDetail.rating = it.mapRating(coupon.rating)
            binding.tvTimeLeft.text = it.mapTimeLeft(coupon.expiredDate)
            binding.tvContentLength.text = it.mapContentLength(coupon.contentLength)
            binding.tvCouponLeft.text = coupon.usesRemaining.toString()
            binding.tvContentLength.text = coupon.contentLength.toString()
            binding.tvCourseLevel.text = coupon.level
            binding.tvLanguage.text = coupon.language
            binding.tvCourseDescription.text = it.mapHTMLContent(coupon.description)
            binding.tvCourseHeadingTitle.text = coupon.heading
            binding.tvNumberOfStudent.text = it.mapNumberOfStudent(coupon.students)
            binding.tvNumberOfReview.text = it.mapNumberOfReview(coupon.reviews)
            binding.tvCourseTitle.text = coupon.title
            binding.tvCourseAuthor.text = coupon.author
            binding.tvCourseCategory.text = coupon.category
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

        binding.btnOpenBrowser.setOnClickListener {
            openLinkInBrowser(this, coupon.couponUrl)
        }

        binding.btnShare.setOnClickListener {
            shareLink(this, coupon.title, coupon.couponUrl)
        }
    }

    private fun showFetchingErrorDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(this.getString(R.string.coupon_detail_error_title))
            .setMessage(this.getString(R.string.error_fetching_null_coupon))
            .setNeutralButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .create().show()
    }

    private fun showNoInternetDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setTitle(R.string.no_internet_title).setMessage(R.string.no_internet_message)
            .setPositiveButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
                couponDetailViewModel.checkIfInternetAvailable()
            }
            .setCancelable(false)
            .create().show()

    }

    private fun shareLink(context: Context, title: String, url: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, url)
        context.startActivity(Intent.createChooser(intent, title))
    }

    private fun openLinkInBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }

    private fun showMainContentView() {
        binding.mlCouponDetail.isVisible = true
    }

    private fun hideLoadingProgressbar() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun showLoadingProgressbar() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog.newInstance(getString(R.string.dialog_loading_text))
            loadingDialog?.show(supportFragmentManager, "loading")
        }
    }
}
