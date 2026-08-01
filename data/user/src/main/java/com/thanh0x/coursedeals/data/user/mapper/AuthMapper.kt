package com.thanh0x.coursedeals.data.user.mapper

import com.thanh0x.coursedeals.data.user.model.PostAuthenticationData
import com.thanh0x.coursedeals.data.user.model.TokenResponseData
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData

fun TokenResponseData.toDomain(): TokenData {
    return TokenData(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType
    )
}

fun AuthCredentials.toDto(): PostAuthenticationData {
    return PostAuthenticationData(
        username = username,
        password = password
    )
}
