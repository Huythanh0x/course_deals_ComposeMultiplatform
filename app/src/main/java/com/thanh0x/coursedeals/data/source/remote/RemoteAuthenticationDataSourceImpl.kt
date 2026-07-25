package com.batdaulaptrinh.freeudemycoupons.data.source.remote

import com.batdaulaptrinh.freeudemycoupons.data.model.PostAuthenticationData
import com.batdaulaptrinh.freeudemycoupons.data.model.TokenResponseData
import com.batdaulaptrinh.freeudemycoupons.domain.source.RemoteAuthenticationDataSource
import retrofit2.Response

class RemoteAuthenticationDataSourceImpl(private val userAuthenticationService: UserAuthenticationService) :
    RemoteAuthenticationDataSource {
    override suspend fun register(postAuthenticationData: PostAuthenticationData): Response<Any> {
        return userAuthenticationService.register(postAuthenticationData)
    }

    override suspend fun login(postAuthenticationData: PostAuthenticationData): Response<TokenResponseData> {
        return userAuthenticationService.login(postAuthenticationData)
    }

    override suspend fun checkIfTokeExpired() = userAuthenticationService.checkIfTokeExpired()
    override suspend fun requestFingerprintToken() =
        userAuthenticationService.requestFingerprintToken()

    override suspend fun requestAccessToken() = userAuthenticationService.requestAccessToken()
}
