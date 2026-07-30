package com.thanh0x.coursedeals.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.thanh0x.coursedeals.data.mapper.toEntity
import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.data.model.RemoteKey
import com.thanh0x.coursedeals.domain.model.CouponMetadata
import com.thanh0x.coursedeals.data.source.local.CouponDatabase
import com.thanh0x.coursedeals.data.source.remote.CouponService
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class CouponRemoteMediator(
    private val database: CouponDatabase,
    private val couponService: CouponService,
    private val query: String? = null,
    private val onMetadataLoaded: (suspend (CouponMetadata) -> Unit)? = null
) : RemoteMediator<Int, Coupon>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Coupon>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 0
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val apiResponse = if (query.isNullOrBlank()) {
                couponService.fetchPagedCoupons(page, state.config.pageSize)
            } else {
                couponService.searchCourseCoupon(query, page, state.config.pageSize)
            }

            val body = apiResponse.body()
            val endOfPaginationReached = body == null || body.courses.isEmpty() || page >= body.totalPage - 1

            if (body != null) {
                onMetadataLoaded?.invoke(
                    CouponMetadata(
                        body.totalCoupon,
                        body.lastFetchTime,
                        System.currentTimeMillis()
                    )
                )
            }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeyDao().clearRemoteKeys()
                    database.couponDao().clearAllCoupons()
                }

                val prevKey = if (page == 0) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = body?.courses?.map {
                    RemoteKey(courseId = it.courseId, prevKey = prevKey, nextKey = nextKey)
                } ?: emptyList<RemoteKey>()

                val coupons = body?.courses?.map { dto ->
                    dto.toEntity()
                } ?: emptyList()

                database.remoteKeyDao().insertAll(keys)
                database.couponDao().insertCoupons(coupons)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            Timber.e(exception, "RemoteMediator Error")
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Coupon>): RemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { coupon ->
                database.remoteKeyDao().remoteKeysCourseId(coupon.courseId)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Coupon>): RemoteKey? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { coupon ->
                database.remoteKeyDao().remoteKeysCourseId(coupon.courseId)
            }
    }

    private suspend fun getRemoteKeyClosestToPosition(state: PagingState<Int, Coupon>): RemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.courseId?.let { courseId ->
                database.remoteKeyDao().remoteKeysCourseId(courseId)
            }
        }
    }
}
