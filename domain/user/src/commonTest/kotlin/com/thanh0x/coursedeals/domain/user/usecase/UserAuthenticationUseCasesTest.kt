package com.thanh0x.coursedeals.domain.user.usecase

import com.thanh0x.coursedeals.core.common.AppResult
import com.thanh0x.coursedeals.domain.user.AuthCredentials
import com.thanh0x.coursedeals.domain.user.TokenData
import com.thanh0x.coursedeals.domain.user.UserAuthenticationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserAuthenticationUseCasesTest {

    private lateinit var fakeRepo: FakeUserAuthenticationRepository

    @BeforeTest
    fun setUp() {
        fakeRepo = FakeUserAuthenticationRepository()
    }

    @Test
    fun sendOtpUseCase_invokesRepositoryWithEmail() = runTest {
        val useCase = SendOtpUseCase(fakeRepo)

        val result = useCase(email = "otpuser@example.com")

        assertTrue(result is AppResult.Success)
        assertEquals("OTP sent to email", result.data)
        assertEquals("otpuser@example.com", fakeRepo.lastSentOtpEmail)
    }

    @Test
    fun registerUseCase_invokesRepositoryWithEmailPasswordAndFullName() = runTest {
        val useCase = RegisterUseCase(fakeRepo)

        val result = useCase(
            email = "newuser@example.com",
            password = "password123",
            fullName = "New User"
        )

        assertTrue(result is AppResult.Success)
        assertEquals("OTP sent to email", result.data)
        assertEquals("newuser@example.com", fakeRepo.lastRegisteredEmail)
        assertEquals("password123", fakeRepo.lastRegisteredPassword)
        assertEquals("New User", fakeRepo.lastRegisteredFullName)
    }

    @Test
    fun verifyOtpUseCase_invokesRepositoryWithEmailAndOtp() = runTest {
        val useCase = VerifyOtpUseCase(fakeRepo)

        val result = useCase(email = "newuser@example.com", otp = "000000")

        assertTrue(result is AppResult.Success)
        val tokenData = result.data
        assertEquals("access-otp-token", tokenData.accessToken)
        assertEquals("refresh-otp-token", tokenData.refreshToken)
        assertEquals("newuser@example.com", fakeRepo.lastVerifiedEmail)
        assertEquals("000000", fakeRepo.lastVerifiedOtp)
    }

    @Test
    fun loginUseCase_invokesRepositoryWithCredentials() = runTest {
        val useCase = LoginUseCase(fakeRepo)

        val result = useCase(username = "user@example.com", password = "secret")

        assertTrue(result is AppResult.Success)
        val tokenData = result.data
        assertEquals("access-login-token", tokenData.accessToken)
        assertEquals("refresh-login-token", tokenData.refreshToken)
        assertEquals(AuthCredentials("user@example.com", "secret"), fakeRepo.lastLoginCredentials)
    }

    @Test
    fun saveAndClearTokens_updatesAndClearsBothAccessAndRefreshTokens() = runTest {
        val saveAccessUseCase = SaveJwtTokenUseCase(fakeRepo)
        val saveRefreshUseCase = SaveRefreshTokenUseCase(fakeRepo)
        val clearAllTokensUseCase = ClearLocalTokenUseCase(fakeRepo)

        saveAccessUseCase("jwt-access-1")
        saveRefreshUseCase("jwt-refresh-1")

        assertEquals("jwt-access-1", fakeRepo.getLocalToken())
        assertEquals("jwt-refresh-1", fakeRepo.getLocalRefreshToken())

        clearAllTokensUseCase()

        assertNull(fakeRepo.getLocalToken())
        assertNull(fakeRepo.getLocalRefreshToken())
    }

    private class FakeUserAuthenticationRepository : UserAuthenticationRepository {
        var lastLoginCredentials: AuthCredentials? = null
        var lastRegisteredEmail: String? = null
        var lastRegisteredPassword: String? = null
        var lastRegisteredFullName: String? = null
        var lastSentOtpEmail: String? = null
        var lastVerifiedEmail: String? = null
        var lastVerifiedOtp: String? = null

        private var accessToken: String? = null
        private var refreshToken: String? = null
        private var cipherText: String? = null

        override suspend fun sendOtp(email: String): AppResult<String> {
            lastSentOtpEmail = email
            return AppResult.Success("OTP sent to email")
        }

        override suspend fun login(credentials: AuthCredentials): AppResult<TokenData> {
            lastLoginCredentials = credentials
            return AppResult.Success(
                TokenData(
                    accessToken = "access-login-token",
                    refreshToken = "refresh-login-token",
                    tokenType = "Bearer"
                )
            )
        }

        override suspend fun register(
            email: String,
            password: String,
            fullName: String?
        ): AppResult<String> {
            lastRegisteredEmail = email
            lastRegisteredPassword = password
            lastRegisteredFullName = fullName
            return AppResult.Success("OTP sent to email")
        }

        override suspend fun verifyOtp(email: String, otp: String): AppResult<TokenData> {
            lastVerifiedEmail = email
            lastVerifiedOtp = otp
            return AppResult.Success(
                TokenData(
                    accessToken = "access-otp-token",
                    refreshToken = "refresh-otp-token",
                    tokenType = "Bearer"
                )
            )
        }

        override suspend fun refreshToken(refreshToken: String): AppResult<TokenData> {
            return AppResult.Success(
                TokenData(
                    accessToken = "new-access-token",
                    refreshToken = "new-refresh-token",
                    tokenType = "Bearer"
                )
            )
        }

        override suspend fun getLocalToken(): String? = accessToken
        override suspend fun saveLocalToken(token: String) { accessToken = token }
        override suspend fun clearLocalToken() { accessToken = null }
        override suspend fun checkIfTokenExpired(): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun getCipherTextWrapper(): String? = cipherText
        override suspend fun saveCipherTextWrapper(fingerprintToken: String) { cipherText = fingerprintToken }
        override suspend fun clearCipherTextWrapper() { cipherText = null }
        override suspend fun getLocalRefreshToken(): String? = refreshToken
        override suspend fun saveLocalRefreshToken(refreshToken: String) { this.refreshToken = refreshToken }
        override suspend fun clearLocalRefreshToken() { refreshToken = null }
        override suspend fun requestFingerprintToken(): AppResult<TokenData> =
            AppResult.Success(TokenData(accessToken = "fp-token"))
        override suspend fun requestAccessToken(): AppResult<TokenData> =
            AppResult.Success(TokenData(accessToken = "access-token"))
        override suspend fun socialLogin(provider: String, idToken: String): AppResult<TokenData> =
            AppResult.Success(TokenData(accessToken = "social-token", refreshToken = "social-refresh"))
    }
}
