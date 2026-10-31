package com.thanh0x.coursedeals.domain.user.di

import com.thanh0x.coursedeals.domain.user.usecase.CheckIfTokenExpiredUseCase
import com.thanh0x.coursedeals.domain.user.usecase.ClearCachedUserProfileUseCase
import com.thanh0x.coursedeals.domain.user.usecase.ClearLocalRefreshTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.ClearLocalTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.CryptographyManagerUseCase
import com.thanh0x.coursedeals.domain.user.usecase.GetLocalTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.GetUserProfileUseCase
import com.thanh0x.coursedeals.domain.user.usecase.LoadCipherTextWrapperUseCase
import com.thanh0x.coursedeals.domain.user.usecase.LoadDarkModeUseCase
import com.thanh0x.coursedeals.domain.user.usecase.LoadFingerprintUseCase
import com.thanh0x.coursedeals.domain.user.usecase.LoginUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RegisterUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RequestAccessTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.RequestFingerprintTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveCipherTextWrapperUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveDarkModeUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveFingerprintUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveJwtTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SaveRefreshTokenUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingFingerprintUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SettingUserProfileUseCase
import com.thanh0x.coursedeals.domain.user.usecase.SocialLoginUseCase
import com.thanh0x.coursedeals.domain.user.usecase.UpdateUserProfileUseCase
import com.thanh0x.coursedeals.domain.user.usecase.VerifyOtpUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val userDomainModule = module {
    factoryOf(::CheckIfTokenExpiredUseCase)
    factoryOf(::ClearCachedUserProfileUseCase)
    factoryOf(::ClearLocalRefreshTokenUseCase)
    factoryOf(::ClearLocalTokenUseCase)
    factoryOf(::CryptographyManagerUseCase)
    factoryOf(::GetLocalTokenUseCase)
    factoryOf(::GetUserProfileUseCase)
    factoryOf(::LoadCipherTextWrapperUseCase)
    factoryOf(::LoadDarkModeUseCase)
    factoryOf(::LoadFingerprintUseCase)
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::RequestAccessTokenUseCase)
    factoryOf(::RequestFingerprintTokenUseCase)
    factoryOf(::SaveCipherTextWrapperUseCase)
    factoryOf(::SaveDarkModeUseCase)
    factoryOf(::SaveFingerprintUseCase)
    factoryOf(::SaveJwtTokenUseCase)
    factoryOf(::SaveRefreshTokenUseCase)
    factoryOf(::SettingFingerprintUseCase)
    factoryOf(::SettingUserProfileUseCase)
    factoryOf(::SocialLoginUseCase)
    factoryOf(::UpdateUserProfileUseCase)
    factoryOf(::VerifyOtpUseCase)
}
