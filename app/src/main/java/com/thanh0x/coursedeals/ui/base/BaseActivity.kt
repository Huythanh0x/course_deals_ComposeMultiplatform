package com.thanh0x.coursedeals.ui.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.ui.custom_view.LoadingDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Long.max
import kotlin.time.Duration.Companion.milliseconds

abstract class BaseActivity : AppCompatActivity() {

    private var loadingDialog: LoadingDialog? = null
    private var loadingStartTime: Long = 0L

    fun showLoading(message: String? = null) {
        if (loadingDialog == null) {
            loadingStartTime = System.currentTimeMillis()
            loadingDialog = LoadingDialog.newInstance(message)
            loadingDialog?.show(supportFragmentManager, "loading")
        }
    }

    fun hideLoading() {
        lifecycleScope.launch {
            val elapsedTime = System.currentTimeMillis() - loadingStartTime
            val remainingTime = max(500L - elapsedTime, 0L)
            if (remainingTime > 0) {
                delay(remainingTime.milliseconds)
            }
            loadingDialog?.dismiss()
            loadingDialog = null
        }
    }

    fun showAlertDialog(title: String, message: String, onPositiveClick: (() -> Unit)? = null) {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok_text_button) { dialog, _ ->
                dialog.dismiss()
                onPositiveClick?.invoke()
            }
            .show()
    }
}
