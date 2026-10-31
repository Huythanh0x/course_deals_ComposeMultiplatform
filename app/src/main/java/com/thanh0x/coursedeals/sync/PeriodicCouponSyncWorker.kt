package com.thanh0x.coursedeals.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.data.coupons.sync.CouponSyncManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PeriodicCouponSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val couponSyncManager: CouponSyncManager by inject()

    override suspend fun doWork(): Result {
        Timber.d("Starting periodic coupon sync in background worker")
        return when (val result = couponSyncManager.syncCoupons(force = true)) {
            is AppResult.Success -> {
                Timber.d("Periodic coupon sync completed successfully")
                Result.success()
            }
            is AppResult.Error -> {
                Timber.e("Periodic coupon sync failed: %s", result.message)
                Result.retry()
            }
            is AppResult.Loading -> {
                Result.retry()
            }
        }
    }

    companion object {
        const val WORK_NAME = "PeriodicCouponSyncWork"
        private const val REPEAT_INTERVAL_HOURS = 8L
        private const val FLEX_INTERVAL_HOURS = 2L

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<PeriodicCouponSyncWorker>(
                REPEAT_INTERVAL_HOURS,
                TimeUnit.HOURS,
                FLEX_INTERVAL_HOURS,
                TimeUnit.HOURS,
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest,
            )
            Timber.d("Scheduled PeriodicCouponSyncWork with 8h interval")
        }
    }
}
