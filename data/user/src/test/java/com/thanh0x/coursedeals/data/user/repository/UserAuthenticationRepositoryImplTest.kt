package com.thanh0x.coursedeals.data.user.repository

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.source.LocalAuthenticationDataSource
import com.thanh0x.coursedeals.domain.user.source.RemoteAuthenticationDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserAuthenticationRepositoryImplTest {

    private lateinit var fakeLocalDataSource: FakeLocalAuthenticationDataSource
    private lateinit var fakeRemoteDataSource: FakeRemoteAuthenticationDataSource
    private lateinit var repository: UserAuthenticationRepositoryImpl

    @Before
    fun setUp() {
        fakeLocalDataSource = FakeLocalAuthenticationDataSource()
        fakeRemoteDataSource = FakeRemoteAuthenticationDataSource()
        repository = UserAuthenticationRepositoryImpl(fakeLocalDataSource, fakeRemoteDataSource)
    }

    @Test
    fun register_delegatesToRemoteDataSourceAndReturnsMessage() = runBlocking {
        fakeRemoteDataSource.registerResult = AppResult.Success("OTP sent to email")

        val result = repository.register(
            email = "student@coursedeals.com",
            password = "password123",
            fullName = "Student Name"
        )

        assertTrue(result is AppResult.Success)
        assertEquals("OTP sent to email", (result as AppResult.Success).data)
        assertEquals("student@coursedeals.com", fakeRemoteDataSource.lastEmail)
        assertEquals("password123", fakeRemoteDataSource.lastPassword)
        assertEquals("Student Name", fakeRemoteDataSource.lastFullName)
    }

    @Test
    fun verifyOtp_delegatesToRemoteDataSourceAndReturnsTokenData() = runBlocking {
        val expectedToken = TokenData(
            accessToken = "verified-access",
            refreshToken = "verified-refresh",
            tokenType = "Bearer"
        )
        fakeRemoteDataSource.verifyOtpResult = AppResult.Success(expectedToken)

        val result = repository.verifyOtp(email = "student@coursedeals.com", otp = "000000")

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals("verified-access", data.accessToken)
        assertEquals("verified-refresh", data.refreshToken)
        assertEquals("000000", fakeRemoteDataSource.lastOtp)
    }

    @Test
    fun refreshToken_delegatesToRemoteDataSourceAndReturnsRefreshedTokens() = runBlocking {
        val expectedToken = TokenData(
            accessToken = "rotated-access-token",
            refreshToken = "rotated-refresh-token",
            tokenType = "Bearer"
        )
        fakeRemoteDataSource.refreshResult = AppResult.Success(expectedToken)

        val result = repository.refreshToken("old-refresh-token")

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals("rotated-access-token", data.accessToken)
        assertEquals("rotated-refresh-token", data.refreshToken)
        assertEquals("old-refresh-token", fakeRemoteDataSource.lastRefreshToken)
    }

    @Test
    fun localTokenAndRefreshToken_saveGetAndClearPersistCorrectly() = runBlocking {
        repository.saveLocalToken("access-abc")
        repository.saveLocalRefreshToken("refresh-xyz")

        assertEquals("access-abc", repository.getLocalToken())
        assertEquals("refresh-xyz", repository.getLocalRefreshToken())

        repository.clearLocalToken()
        repository.clearLocalRefreshToken()

        assertNull(repository.getLocalToken())
        assertNull(repository.getLocalRefreshToken())
    }

    private class FakeLocalAuthenticationDataSource : LocalAuthenticationDataSource {
        private var accessToken: String? = null
        private var refreshToken: String? = null
        private var cipherText: String? = null

        override suspend fun getLocalToken(): String? = accessToken
        override suspend fun saveLocalToken(newLoginToken: String) { accessToken = newLoginToken }
        override suspend fun clearLocalToken() { accessToken = null }
        override suspend fun getCipherTextWrapper(): String? = cipherText
        override suspend fun saveCipherTextWrapper(newLoginToken: String) { cipherText = newLoginToken }
        override suspend fun clearCipherTextWrapper() { cipherText = null }
        override suspend fun getLocalRefreshToken(): String? = refreshToken
        override suspend fun saveLocalRefreshToken(newLoginToken: String) { refreshToken = newLoginToken }
        override suspend fun clearLocalRefreshToken() { refreshToken = null }
    }

    private class FakeRemoteAuthenticationDataSource : RemoteAuthenticationDataSource {
        var lastEmail: String? = null
        var lastPassword: String? = null
        var lastFullName: String? = null
        var lastOtp: String? = null
        var lastRefreshToken: String? = null

        var registerResult: AppResult<String> = AppResult.Error("Not configured")
        var verifyOtpResult: AppResult<TokenData> = AppResult.Error("Not configured")
        var refreshResult: AppResult<TokenData> = AppResult.Error("Not configured")
        var loginResult: AppResult<TokenData> = AppResult.Error("Not configured")

        override suspend fun register(
            email: String,
            password: String,
            fullName: String?
        ): AppResult<String> {
            lastEmail = email
            lastPassword = password
            lastFullName = fullName
            return registerResult
        }

        override suspend fun verifyOtp(email: String, otp: String): AppResult<TokenData> {
            lastEmail = email
            lastOtp = otp
            return verifyOtpResult
        }

        override suspend fun refreshToken(refreshToken: String): AppResult<TokenData> {
            lastRefreshToken = refreshToken
            return refreshResult
        }

        override suspend fun login(credentials: AuthCredentials): AppResult<TokenData> = loginResult
        override suspend fun checkIfTokenExpired(): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun requestFingerprintToken(): AppResult<TokenData> = loginResult
        override suspend fun requestAccessToken(): AppResult<TokenData> = loginResult
        override suspend fun socialLogin(provider: String, idToken: String): AppResult<TokenData> = loginResult
    }
}
