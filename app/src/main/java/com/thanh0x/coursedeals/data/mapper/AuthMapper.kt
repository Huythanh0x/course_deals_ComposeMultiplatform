package com.thanh0x.coursedeals.data.mapper

import com.thanh0x.coursedeals.data.model.PostAuthenticationData
import com.thanh0x.coursedeals.data.model.TokenResponseData
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
