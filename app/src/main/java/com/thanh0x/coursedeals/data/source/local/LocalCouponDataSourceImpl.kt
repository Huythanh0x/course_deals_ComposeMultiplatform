package com.thanh0x.coursedeals.data.source.local

import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery
import com.thanh0x.coursedeals.data.model.Coupon
import com.thanh0x.coursedeals.data.source.LocalCouponDataSource
import com.thanh0x.coursedeals.domain.model.CourseLanguage
import com.thanh0x.coursedeals.domain.model.FilterData
import com.thanh0x.coursedeals.domain.model.SortOption
import com.thanh0x.coursedeals.util.Constant
import javax.inject.Inject

class LocalCouponDataSourceImpl @Inject constructor(private val couponDao: CouponDao) :
    LocalCouponDataSource {
    override suspend fun getAllCoupons(): List<Coupon> = couponDao.getAllCoupons()

    override fun getPagingCoupons(): PagingSource<Int, Coupon> = couponDao.getPagingCoupons()

    override fun getFilteredCoupons(query: String?, filter: FilterData): PagingSource<Int, Coupon> {
        val (whereClause, args) = buildConditions(query, filter)

        // Sort By
        val orderClause = when (filter.sortBy) {
            SortOption.RATING -> "ORDER BY rating DESC"
            SortOption.STUDENTS -> "ORDER BY students DESC"
            SortOption.REVIEWS -> "ORDER BY reviews DESC"
            SortOption.EXPIRING_SOON -> "ORDER BY expired_time ASC"
            SortOption.NEWEST -> "ORDER BY created_at DESC"
        }

        val sql = "SELECT * FROM ${Constant.COUPON_TABLE_NAME} $whereClause $orderClause"
        val sqliteQuery = SimpleSQLiteQuery(sql, args.toTypedArray())

        return couponDao.getFilteredCoupons(sqliteQuery)
    }

    override fun getFilteredCount(query: String?, filter: FilterData): kotlinx.coroutines.flow.Flow<Int> {
        val (whereClause, args) = buildConditions(query, filter)
        val sql = "SELECT COUNT(*) FROM ${Constant.COUPON_TABLE_NAME} $whereClause"
        val sqliteQuery = SimpleSQLiteQuery(sql, args.toTypedArray())

        return couponDao.getFilteredCount(sqliteQuery)
    }

    override suspend fun getSearchSuggestions(query: String): List<String> =
        couponDao.getSearchSuggestions(query.lowercase())

    private fun buildConditions(query: String?, filter: FilterData): Pair<String, List<Any>> {
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
            args.addAll(filter.categories.map { it.dbValue })
        }

        // Language
        when (filter.language) {
            CourseLanguage.ENGLISH -> {
                conditions.add("LOWER(language) = ?")
                args.add("english")
            }
            CourseLanguage.OTHERS -> {
                conditions.add("(LOWER(language) != ? OR language IS NULL)")
                args.add("english")
            }
            CourseLanguage.ALL -> { /* No condition */ }
        }

        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE " + conditions.joinToString(" AND ")
        } else {
            ""
        }

        return whereClause to args
    }

    override suspend fun insertCoupon(coupon: Coupon) = couponDao.insertCoupon(coupon)

    override suspend fun insertCoupons(coupons: List<Coupon>) = couponDao.insertCoupons(coupons)

    override suspend fun clearALlCoupons() = couponDao.clearAllCoupons()
}
