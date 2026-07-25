package com.thanh0x.coursedeals.domain.usecase.authentication

import android.content.Context
import com.thanh0x.coursedeals.R
import com.thanh0x.coursedeals.data.model.PostAuthenticationData
import com.thanh0x.coursedeals.data.model.TokenResponseData
import com.thanh0x.coursedeals.data.repository.UserAuthenticationRepositoryImpl
import com.thanh0x.coursedeals.util.NetWorkResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userAuthenticationRepositoryImpl: UserAuthenticationRepositoryImpl
) {
    suspend operator fun invoke(
        username: String,
        password: String
    ): NetWorkResult<TokenResponseData> {
        val response =
            userAuthenticationRepositoryImpl.login(PostAuthenticationData(username, password))
        return when (response.code()) {
            500 -> NetWorkResult.Error(context.getString(R.string.error_status_code_500))
            401 -> NetWorkResult.Error(context.getString(R.string.error_status_code_401))
            400 -> NetWorkResult.Error(context.getString(R.string.error_status_code_400, response.message()))
            200 -> {
                if (response.body() != null && response.body() is TokenResponseData) {
                    return NetWorkResult.Success(response.body())
                } else {
                    return NetWorkResult.Error(context.getString(R.string.error_token_is_null))
                }
            }
            else -> NetWorkResult.Error(context.getString(R.string.error_unknown_status_code, response.code().toString()))
        }
    }
}