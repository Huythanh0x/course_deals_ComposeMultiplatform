package com.batdaulaptrinh.freeudemycoupons.data.source.remote

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.batdaulaptrinh.freeudemycoupons.data.model.Coupon
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val STARTING_KEY = 0
private const val LOAD_DELAY_MILLIS = 500L

class RemotePagingCouponDataSourceImpl @Inject constructor(private val couponService: CouponService) :
    PagingSource<Int, Coupon>() {
    override fun getRefreshKey(state: PagingState<Int, Coupon>): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Coupon> {
        val pageNumber = params.key ?: STARTING_KEY
        val pageSize = params.loadSize
        try {
            val responseData = couponService.fetchPagedCoupons(pageNumber, pageSize)
            responseData.body()?.let {
                val pageMax = it.totalPage
                val courses = it.courses
                Log.d("CURRENT PAGE", "$pageNumber $pageMax with $pageSize")
                if (pageNumber != STARTING_KEY) delay(LOAD_DELAY_MILLIS)
                return LoadResult.Page(
                    courses,
                    prevKey = if (pageNumber > 1) pageNumber - 1 else null,
                    nextKey = if (pageNumber < pageMax) pageNumber + 1 else null
                )
            }
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
        Log.e("LOADING NEW PAGE", "Exception in loading new page")
        return LoadResult.Error(Throwable("Unknown error while loading new paging data"))
    }

    override val keyReuseSupported: Boolean
        get() = true
}