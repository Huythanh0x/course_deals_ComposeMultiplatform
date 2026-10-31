package com.thanh0x.coursedeals.data.coupons.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.data.coupons.repository.CouponRepositoryImpl
import com.thanh0x.coursedeals.data.coupons.repository.SearchRepositoryImpl
import com.thanh0x.coursedeals.data.coupons.source.LocalCouponDataSource
import com.thanh0x.coursedeals.data.coupons.source.local.CouponDatabase
import com.thanh0x.coursedeals.data.coupons.source.local.LocalCouponDataSourceImpl
import com.thanh0x.coursedeals.data.coupons.source.remote.CouponService
import com.thanh0x.coursedeals.data.coupons.source.remote.CouponServiceImpl
import com.thanh0x.coursedeals.data.coupons.source.remote.RemoteCouponDataSourceImpl
import com.thanh0x.coursedeals.domain.coupons.CouponRepository
import com.thanh0x.coursedeals.domain.coupons.SearchRepository
import com.thanh0x.coursedeals.domain.coupons.source.RemoteCouponDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val migration6to7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${Constant.COUPON_TABLE_NAME} ADD COLUMN cached_at INTEGER DEFAULT NULL")
    }
}

val couponDataModule = module {
    single<CouponDatabase> {
        Room.databaseBuilder(androidContext(), CouponDatabase::class.java, Constant.COUPON_DATABASE_NAME)
            .addMigrations(migration6to7)
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single { get<CouponDatabase>().couponDao() }
    single { get<CouponDatabase>().searchHistoryDao() }

    single<CouponService> { CouponServiceImpl(httpClient = get()) }
    single<LocalCouponDataSource> { LocalCouponDataSourceImpl(couponDao = get()) }
    single<RemoteCouponDataSource> { RemoteCouponDataSourceImpl(couponService = get()) }

    single<SearchRepository> {
        SearchRepositoryImpl(
            localCouponDataSource = get(),
            couponDatabase = get()
        )
    }

    single<CouponRepository> {
        CouponRepositoryImpl(
            localCouponDataSource = get(),
            remoteCouponDataSource = get(),
            localSettingsDataSource = get(),
            couponDatabase = get(),
            couponService = get(),
            searchRepository = get()
        )
    }
}
