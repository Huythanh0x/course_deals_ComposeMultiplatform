package com.thanh0x.coursedeals.data.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.thanh0x.coursedeals.core.common.AppVersion
import com.thanh0x.coursedeals.core.common.Constant
import com.thanh0x.coursedeals.data.user.source.remote.AppConfigService
import com.thanh0x.coursedeals.domain.user.FeatureFlagManager
import com.thanh0x.coursedeals.domain.user.model.AppConfig
import com.thanh0x.coursedeals.domain.user.source.LocalAuthenticationDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class FeatureFlagManagerImpl(
    private val dataStore: DataStore<Preferences>,
    private val appConfigService: AppConfigService,
    private val localAuthenticationDataSource: LocalAuthenticationDataSource? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val backoffDelaysMs: List<Long> = DEFAULT_BACKOFF_DELAYS_MS,
    autoRefreshOnInit: Boolean = true,
) : FeatureFlagManager {

    private val snapshotKey = stringPreferencesKey(Constant.PREFERENCES_APP_CONFIG_SNAPSHOT)
    private val loginTokenKey = stringPreferencesKey(Constant.PREFERENCES_LOGIN_TOKEN)

    private val _configFlow = MutableStateFlow(AppConfig())
    override val configFlow: StateFlow<AppConfig> = _configFlow.asStateFlow()

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            loadCachedSnapshot()
            observeAuthStateChanges()
            if (autoRefreshOnInit) {
                launch {
                    performRefreshWithBackoff()
                }
            }
        }
    }

    private suspend fun loadCachedSnapshot() {
        val rawJson = runCatching {
            dataStore.data.first()[snapshotKey]
        }.getOrNull()
        if (!rawJson.isNullOrBlank()) {
            val parsed = parseAppConfigJson(rawJson)
            if (parsed != null) {
                _configFlow.value = parsed
            }
        }
    }

    private fun CoroutineScope.observeAuthStateChanges() {
        val tokenFlow: Flow<String?> = localAuthenticationDataSource?.observeLocalToken()
            ?: dataStore.data.map { preferences -> preferences[loginTokenKey] }
        launch {
            tokenFlow
                .distinctUntilChanged()
                .drop(1)
                .collect {
                    performRefreshWithBackoff()
                }
        }
    }

    override suspend fun refresh(): Result<AppConfig> {
        val deferred = scope.async {
            performRefreshWithBackoff()
        }
        return deferred.await()
    }

    private suspend fun performRefreshWithBackoff(): Result<AppConfig> {
        val firstAttempt = fetchAndPersistOnce()
        if (firstAttempt.isSuccess) {
            return firstAttempt
        }
        var lastResult = firstAttempt
        for (delayMs in backoffDelaysMs) {
            delay(delayMs)
            val retryAttempt = fetchAndPersistOnce()
            if (retryAttempt.isSuccess) {
                return retryAttempt
            }
            lastResult = retryAttempt
        }
        return lastResult
    }

    private suspend fun fetchAndPersistOnce(): Result<AppConfig> {
        val response = appConfigService.getAppConfigRawJson()
        if (!response.isSuccessful) {
            return Result.failure(
                IllegalStateException(
                    "Failed to fetch app config (HTTP ${response.code}): ${response.message}"
                )
            )
        }
        val rawJson = response.body
        val parsed = rawJson?.let { parseAppConfigJson(it) }
            ?: return Result.failure(
                IllegalArgumentException("Invalid or corrupted app config JSON")
            )

        _configFlow.value = parsed
        runCatching {
            dataStore.edit { preferences ->
                preferences[snapshotKey] = rawJson
            }
        }
        return Result.success(parsed)
    }

    override fun boolean(key: String, default: Boolean): Boolean {
        val raw = _configFlow.value.featureFlags[key] ?: return default
        return when (raw.trim().lowercase()) {
            "true" -> true
            "false" -> false
            else -> default
        }
    }

    override fun double(key: String, default: Double): Double {
        val raw = _configFlow.value.featureFlags[key] ?: return default
        return raw.trim().toDoubleOrNull() ?: default
    }

    override fun string(key: String, default: String): String {
        return _configFlow.value.featureFlags[key] ?: default
    }

    companion object {
        val DEFAULT_BACKOFF_DELAYS_MS: List<Long> = listOf(5_000L, 30_000L, 120_000L)

        private val jsonParser = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        internal fun parseAppConfigJson(rawJson: String): AppConfig? {
            return runCatching {
                val root = jsonParser.parseToJsonElement(rawJson).jsonObject
                if (!root.containsKey("minSupportedVersionCode") && !root.containsKey("featureFlags")) {
                    return null
                }
                val minSupported = root["minSupportedVersionCode"]
                    ?.jsonPrimitive
                    ?.intOrNull
                    ?: AppVersion.DEFAULT_VERSION_CODE
                val latest = root["latestVersionCode"]
                    ?.jsonPrimitive
                    ?.intOrNull
                    ?: AppVersion.DEFAULT_LATEST_VERSION_CODE
                val forceUpdateUrl = root["forceUpdateUrl"]
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.takeIf { it.isNotBlank() }
                    ?: Constant.DEFAULT_FORCE_UPDATE_URL
                val flagsObject = root["featureFlags"]?.jsonObject
                val parsedFlags = buildMap {
                    putAll(AppConfig.DEFAULT_FEATURE_FLAGS)
                    flagsObject?.forEach { (key, element) ->
                        val primitive = element as? JsonPrimitive
                        if (primitive != null) {
                            put(key, primitive.content)
                        }
                    }
                }
                AppConfig(
                    minSupportedVersionCode = minSupported,
                    latestVersionCode = latest,
                    forceUpdateUrl = forceUpdateUrl,
                    featureFlags = parsedFlags,
                )
            }.getOrNull()
        }
    }
}
