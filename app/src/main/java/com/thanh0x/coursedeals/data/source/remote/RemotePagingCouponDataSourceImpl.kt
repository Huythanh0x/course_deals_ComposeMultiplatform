package com.thanh0x.coursedeals.data.source.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.thanh0x.coursedeals.data.mapper.toDomain
import com.thanh0x.coursedeals.domain.model.Coupon
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val STARTING_KEY = 0
private const val LOAD_DELAY_MILLIS = 500L

class RemotePagingCouponDataSourceImpl @Inject constructor(private val couponService: CouponService) :
    PagingSource<Int, Coupon>() {
    override fun getRefreshKey(state: PagingState<Int, Coupon>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Coupon> {
        val pageNumber = params.key ?: STARTING_KEY
        val pageSize = params.loadSize
        
        val result: LoadResult<Int, Coupon> = try {
            val responseData = couponService.fetchPagedCoupons(pageNumber, pageSize)
            val body = responseData.body()
            
            if (responseData.isSuccessful && body != null) {
                val pageMax = body.totalPage
                val courses = body.courses.map { dto -> dto.toDomain() }
                Timber.d("CURRENT PAGE: $pageNumber $pageMax with $pageSize")
                if (pageNumber != STARTING_KEY) delay(LOAD_DELAY_MILLIS.milliseconds)
                LoadResult.Page(
                    courses,
                    prevKey = if (pageNumber > 1) pageNumber - 1 else null,
                    nextKey = if (pageNumber < pageMax) pageNumber + 1 else null
                )
            } else {
                Timber.e("LOADING NEW PAGE: Response body is null or unsuccessful")
                LoadResult.Error(Throwable("Unknown error while loading new paging data"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading paging data: page $pageNumber")
            LoadResult.Error(e)
        }
        return result
    }

    override val keyReuseSupported: Boolean
        get() = true
}
