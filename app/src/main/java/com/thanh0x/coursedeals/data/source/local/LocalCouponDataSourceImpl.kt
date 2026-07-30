package com.thanh0x.coursedeals.data.source.local

import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery
import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.data.source.LocalCouponDataSource
import com.thanh0x.coursedeals.domain.model.FilterData
import com.thanh0x.coursedeals.util.Constant
import javax.inject.Inject

class LocalCouponDataSourceImpl @Inject constructor(private val couponDao: CouponDao) :
    LocalCouponDataSource {
    override suspend fun getAllCoupons(): List<Coupon> = couponDao.getAllCoupons()

    override fun getPagingCoupons(): PagingSource<Int, Coupon> = couponDao.getPagingCoupons()

    override fun getFilteredCoupons(query: String?, filter: FilterData): PagingSource<Int, Coupon> {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<Any>()

        // Text Search
        if (!query.isNullOrBlank()) {
            conditions.add("LOWER(title) LIKE '%' || ? || '%'")
            args.add(query.lowercase())
        }

        // Categories (Multi-select)
        if (filter.categories.isNotEmpty()) {
            val placeholders = filter.categories.joinToString(",") { "?" }
            conditions.add("category IN ($placeholders)")
            args.addAll(filter.categories)
        }

        // Language
        if (filter.language != null && filter.language != "All") {
            if (filter.language == "English") {
                conditions.add("language = ?")
                args.add("English")
            } else if (filter.language == "Others") {
                conditions.add("language != ?")
                args.add("English")
            }
        }

        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE " + conditions.joinToString(" AND ")
        } else {
            ""
        }

        // Sort By
        val orderClause = when (filter.sortBy) {
            "Rating" -> "ORDER BY rating DESC"
            "Students" -> "ORDER BY students DESC"
            "Reviews" -> "ORDER BY reviews DESC"
            "Expiring Soon" -> "ORDER BY expired_date ASC"
            "Newest" -> "ORDER BY created_at DESC"
            else -> "ORDER BY created_at DESC"
        }

        val sql = "SELECT * FROM ${Constant.COUPON_TABLE_NAME} $whereClause $orderClause"
        val sqliteQuery = SimpleSQLiteQuery(sql, args.toTypedArray())

        return couponDao.getFilteredCoupons(sqliteQuery)
    }

    override suspend fun insertCoupon(coupon: Coupon) = couponDao.insertCoupon(coupon)

    override suspend fun insertCoupons(coupons: List<Coupon>) = couponDao.insertCoupons(coupons)

    override suspend fun clearALlCoupons() = couponDao.clearAllCoupons()
}
