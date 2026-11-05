package com.thanh0x.coursedeals.web

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal expect fun currentRuntimeLabel(): String

internal expect fun alternateRuntimeLabel(): String

internal expect fun switchRuntimeTarget()

internal expect fun hideLoadingOverlay()

internal expect fun navigateToUrl(url: String)

internal expect fun openExternalTab(url: String)

internal expect fun encodeUriParam(value: String): String

internal expect suspend fun fetchUrlText(url: String): String

internal expect suspend fun fetchUrlBytes(url: String): ByteArray

internal expect suspend fun postUrlJson(
    url: String,
    jsonBody: String,
): Int

internal expect fun currentTimeMillis(): Long

internal expect fun getLocalStorageItem(key: String): String?

internal expect fun setLocalStorageItem(
    key: String,
    value: String,
)

@Serializable
data class WebCouponSummary(
    val courseId: Int = 0,
    val title: String? = null,
    val category: String? = null,
    val level: String? = null,
    val language: String? = null,
    val author: String? = null,
    val rating: Float = 0f,
    val reviews: Int = 0,
    val students: Int = 0,
    val previewImage: String? = null,
    val expiredTime: Long? = null,
    val createdAt: Long? = null,
    val isNew: Boolean = false,
)

@Serializable
data class WebPagedCoupons(
    val lastFetchTime: Long? = null,
    val totalCoupon: Long? = null,
    val totalPage: Int? = null,
    val currentPage: Int? = null,
    val courses: List<WebCouponSummary> = emptyList(),
)

internal enum class WebTopDestination(val route: String, val label: String) {
    HOME("home", "HOME"),
    COURSE("course", "COURSE"),
    PROFILE("profile", "PROFILE"),
}

private val webJsonParser =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

private const val STORAGE_DARK_MODE = "cd_dark_mode"
private const val STORAGE_BOOKMARKS = "cd_bookmarks"
private const val STORAGE_FAV_CATS = "cd_fav_cats"
private const val STORAGE_FAV_KWS = "cd_fav_kws"
private const val STORAGE_USER_NAME = "cd_user_name"
private const val STORAGE_USER_EMAIL = "cd_user_email"
private const val MAX_SUGGESTIONS = 6

@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod")
fun CourseDealsWebScreen() {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var isDarkMode by remember {
        val saved = getLocalStorageItem(STORAGE_DARK_MODE)
        mutableStateOf(saved?.toBooleanStrictOrNull() ?: true)
    }
    var currentTab by remember { mutableStateOf(WebTopDestination.HOME) }
    var allCoupons by remember { mutableStateOf<List<WebCouponSummary>>(emptyList()) }
    var lastFetchServerTime by remember { mutableStateOf<Long?>(null) }
    var localFetchedTime by remember { mutableStateOf<Long?>(null) }
    var showLocalFetchTime by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var query by remember { mutableStateOf("") }
    var filterData by remember { mutableStateOf(WebFilterData()) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSubmitDealSheet by remember { mutableStateOf(false) }

    var selectedCourseId by remember { mutableStateOf<Int?>(null) }
    var selectedDetail by remember { mutableStateOf<WebCouponDetail?>(null) }
    var isDetailLoading by remember { mutableStateOf(false) }

    var bookmarkedIds by remember {
        val raw = getLocalStorageItem(STORAGE_BOOKMARKS).orEmpty()
        val parsed = raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        mutableStateOf(parsed)
    }
    var favCategories by remember {
        val raw = getLocalStorageItem(STORAGE_FAV_CATS)
        val parsed =
            if (raw == null) {
                listOf("Development", "IT & Software")
            } else {
                raw.split("|").filter { it.isNotBlank() }
            }
        mutableStateOf(parsed)
    }
    var favKeywords by remember {
        val raw = getLocalStorageItem(STORAGE_FAV_KWS)
        val parsed =
            if (raw == null) {
                listOf("Kotlin", "AI", "Spring Boot")
            } else {
                raw.split("|").filter { it.isNotBlank() }
            }
        mutableStateOf(parsed)
    }
    var userName by remember { mutableStateOf(getLocalStorageItem(STORAGE_USER_NAME).orEmpty()) }
    var userEmail by remember { mutableStateOf(getLocalStorageItem(STORAGE_USER_EMAIL).orEmpty()) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var subscribedTopics by remember { mutableStateOf(setOf("all_deals", "hot_deals", "development")) }
    var noticeDialogMessage by remember { mutableStateOf<String?>(null) }

    fun loadAllCoupons() {
        scope.launch {
            isLoading = true
            errorMessage = null
            runCatching {
                val bodyText = fetchUrlText("/api/v1/coupons?pageIndex=0&numberPerPage=300")
                webJsonParser.decodeFromString<WebPagedCoupons>(bodyText)
            }.onSuccess { paged ->
                allCoupons = paged.courses
                lastFetchServerTime = paged.lastFetchTime
                localFetchedTime = currentTimeMillis()
                isLoading = false
            }.onFailure { err ->
                errorMessage = err.message ?: "Failed to load coupons"
                isLoading = false
            }
        }
    }

    fun openCourseDetail(course: WebCouponSummary) {
        selectedCourseId = course.courseId
        selectedDetail =
            WebCouponDetail(
                courseId = course.courseId,
                title = course.title,
                category = course.category,
                level = course.level,
                language = course.language,
                author = course.author,
                rating = course.rating,
                reviews = course.reviews,
                students = course.students,
                previewImage = course.previewImage,
                expiredTime = course.expiredTime,
                isNew = course.isNew,
            )
        isDetailLoading = true
        scope.launch {
            runCatching {
                val detailJson = fetchUrlText("/api/v1/coupons/${course.courseId}")
                webJsonParser.decodeFromString<WebCouponDetail>(detailJson)
            }.onSuccess { fullDetail ->
                selectedDetail = fullDetail
                isDetailLoading = false
            }.onFailure {
                isDetailLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        hideLoadingOverlay()
        loadAllCoupons()
    }

    val filteredCoupons =
        remember(allCoupons, query, filterData) {
            val q = query.trim().lowercase()
            val filtered =
                allCoupons.filter { item ->
                    val matchesQuery =
                        q.isEmpty() ||
                            item.title?.lowercase()?.contains(q) == true ||
                            item.author?.lowercase()?.contains(q) == true ||
                            item.category?.lowercase()?.contains(q) == true
                    val matchesCategory =
                        filterData.categories.isEmpty() ||
                            filterData.categories.any { cat ->
                                item.category?.equals(cat, ignoreCase = true) == true
                            }
                    val matchesRating = item.rating.toDouble() >= filterData.minRating
                    val matchesLang =
                        when (filterData.language) {
                            WebCourseLanguage.ALL -> true
                            WebCourseLanguage.ENGLISH -> item.language?.equals("English", ignoreCase = true) == true
                            WebCourseLanguage.OTHERS -> item.language?.equals("English", ignoreCase = true) == false
                        }
                    matchesQuery && matchesCategory && matchesRating && matchesLang
                }
            when (filterData.sortBy) {
                WebSortOption.NEWEST -> filtered.sortedByDescending { it.createdAt ?: 0L }
                WebSortOption.RATING -> filtered.sortedByDescending { it.rating }
                WebSortOption.STUDENTS -> filtered.sortedByDescending { it.students }
                WebSortOption.REVIEWS -> filtered.sortedByDescending { it.reviews }
                WebSortOption.EXPIRING_SOON -> filtered.sortedBy { it.expiredTime ?: Long.MAX_VALUE }
            }
        }

    val suggestions =
        remember(allCoupons, query) {
            val q = query.trim().lowercase()
            if (q.length < 2) {
                emptyList()
            } else {
                allCoupons
                    .mapNotNull { it.title }
                    .filter { it.lowercase().contains(q) }
                    .distinct()
                    .take(MAX_SUGGESTIONS)
            }
        }

    CourseDealsWebTheme(darkTheme = isDarkMode) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            topBar = {
                WebCmpTopBar(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = {
                        val next = !isDarkMode
                        isDarkMode = next
                        setLocalStorageItem(STORAGE_DARK_MODE, next.toString())
                    },
                    onBackToLanding = { navigateToUrl("/") },
                )
            },
            bottomBar = {
                if (selectedCourseId == null) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        WebTopDestination.entries.forEach { dest ->
                            val selected = currentTab == dest
                            val iconVec =
                                when (dest) {
                                    WebTopDestination.HOME ->
                                        if (selected) Icons.Filled.Home else Icons.Outlined.Home
                                    WebTopDestination.COURSE ->
                                        if (selected) CmpIcons.SchoolFilled else CmpIcons.SchoolOutlined
                                    WebTopDestination.PROFILE ->
                                        if (selected) Icons.Filled.Person else Icons.Outlined.Person
                                }
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = dest },
                                icon = { Icon(imageVector = iconVec, contentDescription = dest.label) },
                                label = { Text(dest.label, fontWeight = FontWeight.SemiBold) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                val activeCourseId = selectedCourseId
                if (activeCourseId != null) {
                    val isBookmarked = bookmarkedIds.contains(activeCourseId)
                    WebCouponDetailScreen(
                        detail = selectedDetail,
                        isLoading = isDetailLoading,
                        isBookmarked = isBookmarked,
                        onBackClick = {
                            selectedCourseId = null
                            selectedDetail = null
                        },
                        onBookmarkToggle = {
                            val updated =
                                if (isBookmarked) {
                                    bookmarkedIds - activeCourseId
                                } else {
                                    bookmarkedIds + activeCourseId
                                }
                            bookmarkedIds = updated
                            setLocalStorageItem(STORAGE_BOOKMARKS, updated.joinToString(","))
                        },
                        onShareClick = { _, url ->
                            clipboardManager.setText(AnnotatedString(url))
                        },
                        onEnrollClick = { targetUrl ->
                            openExternalTab(targetUrl)
                        },
                        onReportSubmitted = { reason, otherDetails ->
                            scope.launch {
                                val escapedDetails = (otherDetails ?: "").replace("\"", "\\\"")
                                val payload = """{"reason":"$reason","otherDetails":"$escapedDetails"}"""
                                runCatching {
                                    postUrlJson("/api/courses/$activeCourseId/reports", payload)
                                }
                            }
                        },
                    )
                } else {
                    when (currentTab) {
                        WebTopDestination.HOME -> {
                            WebCmpHomeScreen(
                                allDealsCount = allCoupons.size,
                                filteredCoupons = filteredCoupons,
                                query = query,
                                suggestions = suggestions,
                                filterData = filterData,
                                isLoading = isLoading,
                                errorMessage = errorMessage,
                                showLocalFetchTime = showLocalFetchTime,
                                timestampMillis =
                                    if (showLocalFetchTime) localFetchedTime else lastFetchServerTime,
                                onQueryChange = { query = it },
                                onFilterClick = { showFilterSheet = true },
                                onSubmitDealClick = { showSubmitDealSheet = true },
                                onClearFiltersClick = {
                                    query = ""
                                    filterData = WebFilterData()
                                },
                                onToggleTimestampClick = { showLocalFetchTime = !showLocalFetchTime },
                                onCouponClick = { openCourseDetail(it) },
                                onRetry = { loadAllCoupons() },
                            )
                        }
                        WebTopDestination.COURSE -> {
                            val bookmarkedList =
                                remember(allCoupons, bookmarkedIds) {
                                    allCoupons.filter { bookmarkedIds.contains(it.courseId) }
                                }
                            WebCourseScreen(
                                bookmarkedCourses = bookmarkedList,
                                onCouponClick = { openCourseDetail(it) },
                                onOpenUdemyMyCourses = {
                                    openExternalTab("https://www.udemy.com/home/my-courses/learning/")
                                },
                            )
                        }
                        WebTopDestination.PROFILE -> {
                            WebProfileScreen(
                                displayName = userName,
                                email = userEmail,
                                isSignedIn = userEmail.isNotBlank(),
                                favCategories = favCategories,
                                favKeywords = favKeywords,
                                isDarkModeEnabled = isDarkMode,
                                isNotificationsEnabled = isNotificationsEnabled,
                                subscribedTopics = subscribedTopics,
                                onAddCategory = { cat ->
                                    if (!favCategories.contains(cat)) {
                                        val next = favCategories + cat
                                        favCategories = next
                                        setLocalStorageItem(STORAGE_FAV_CATS, next.joinToString("|"))
                                    }
                                },
                                onRemoveCategory = { cat ->
                                    val next = favCategories - cat
                                    favCategories = next
                                    setLocalStorageItem(STORAGE_FAV_CATS, next.joinToString("|"))
                                },
                                onAddKeyword = { kw ->
                                    if (!favKeywords.contains(kw)) {
                                        val next = favKeywords + kw
                                        favKeywords = next
                                        setLocalStorageItem(STORAGE_FAV_KWS, next.joinToString("|"))
                                    }
                                },
                                onRemoveKeyword = { kw ->
                                    val next = favKeywords - kw
                                    favKeywords = next
                                    setLocalStorageItem(STORAGE_FAV_KWS, next.joinToString("|"))
                                },
                                onDarkModeToggled = { enabled ->
                                    isDarkMode = enabled
                                    setLocalStorageItem(STORAGE_DARK_MODE, enabled.toString())
                                },
                                onNotificationsToggled = { isNotificationsEnabled = it },
                                onTopicToggled = { topicId, enabled ->
                                    subscribedTopics =
                                        if (enabled) {
                                            subscribedTopics + topicId
                                        } else {
                                            subscribedTopics - topicId
                                        }
                                },
                                onSignInSuccess = { name, email ->
                                    userName = name
                                    userEmail = email
                                    setLocalStorageItem(STORAGE_USER_NAME, name)
                                    setLocalStorageItem(STORAGE_USER_EMAIL, email)
                                },
                                onLogoutClick = {
                                    userName = ""
                                    userEmail = ""
                                    setLocalStorageItem(STORAGE_USER_NAME, "")
                                    setLocalStorageItem(STORAGE_USER_EMAIL, "")
                                },
                            )
                        }
                    }
                }
            }
        }

        if (showFilterSheet) {
            WebFilterBottomSheet(
                initialFilter = filterData,
                onDismissRequest = { showFilterSheet = false },
                onApply = { updated -> filterData = updated },
            )
        }

        if (showSubmitDealSheet) {
            WebSubmitDealBottomSheet(
                onDismissRequest = { showSubmitDealSheet = false },
                onSubmit = { submittedUrl ->
                    scope.launch {
                        val escaped = submittedUrl.replace("\"", "\\\"")
                        runCatching {
                            postUrlJson("/api/v1/coupons", """{"couponUrl":"$escaped"}""")
                        }
                        noticeDialogMessage = "Deal submitted successfully! It will be reviewed soon."
                    }
                },
            )
        }

        val currentNotice = noticeDialogMessage
        if (currentNotice != null) {
            AlertDialog(
                onDismissRequest = { noticeDialogMessage = null },
                title = { Text("Notice") },
                text = { Text(currentNotice) },
                confirmButton = {
                    TextButton(onClick = { noticeDialogMessage = null }) {
                        Text("OK")
                    }
                },
            )
        }
    }
}

@Composable
private fun WebCmpTopBar(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onBackToLanding: () -> Unit,
) {
    val activeEngine = currentRuntimeLabel()
    val otherEngine = alternateRuntimeLabel()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        imageVector = CmpIcons.LogoMark,
                        contentDescription = "Course Deal",
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        text = "Course Deal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "Engine: $activeEngine",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalButton(
                        onClick = { switchRuntimeTarget() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                    ) {
                        Text(
                            text = "Switch to $otherEngine",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    OutlinedButton(
                        onClick = onToggleDarkMode,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                    ) {
                        Text(
                            text = if (isDarkMode) "Light Theme" else "Dark Theme",
                            fontSize = 12.sp,
                        )
                    }
                    OutlinedButton(
                        onClick = onBackToLanding,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                    ) {
                        Text("<- Landing Page", fontSize = 12.sp)
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
@Suppress("LongParameterList", "LongMethod")
private fun WebCmpHomeScreen(
    allDealsCount: Int,
    filteredCoupons: List<WebCouponSummary>,
    query: String,
    suggestions: List<String>,
    filterData: WebFilterData,
    isLoading: Boolean,
    errorMessage: String?,
    showLocalFetchTime: Boolean,
    timestampMillis: Long?,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSubmitDealClick: () -> Unit,
    onClearFiltersClick: () -> Unit,
    onToggleTimestampClick: () -> Unit,
    onCouponClick: (WebCouponSummary) -> Unit,
    onRetry: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }
    val isFiltered = query.isNotBlank() || filterData.isActive
    val hasActiveFilters = filterData.isActive

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    focusManager.clearFocus()
                    isSearchFocused = false
                },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Search Bar + Filter Button (exact HomeScreen.kt layout)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .onFocusChanged { isSearchFocused = it.isFocused },
                    placeholder = {
                        Text(
                            text = "Search courses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions =
                        KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                isSearchFocused = false
                            },
                        ),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    FilledTonalIconButton(
                        onClick = {
                            focusManager.clearFocus()
                            isSearchFocused = false
                            onFilterClick()
                        },
                        modifier = Modifier.size(48.dp),
                        colors =
                            IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Icon(
                            imageVector = CmpIcons.FilterList,
                            contentDescription = "Filter",
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    if (hasActiveFilters) {
                        Box(
                            modifier =
                                Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error),
                        )
                    }
                }
            }

            // Stat deals row + action buttons (exact HomeScreen.kt layout)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dealsText =
                        if (isFiltered) {
                            "Matching ${filteredCoupons.size}/$allDealsCount deals"
                        } else {
                            "$allDealsCount deals"
                        }
                    Text(
                        text = dealsText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (!isFiltered) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val prefix = if (showLocalFetchTime) "· Fetched " else "· Updated "
                        Text(
                            text = prefix + formatTimeAgo(timestampMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable(onClick = onToggleTimestampClick),
                        )
                    }
                }

                if (isFiltered) {
                    FilledTonalButton(
                        onClick = onClearFiltersClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    FilledTonalButton(
                        onClick = onSubmitDealClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Image(
                            imageVector = CmpIcons.LogoMark,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Submit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Content Grid
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
            ) {
                when {
                    isLoading && allDealsCount == 0 -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Loading verified 100% off coupons...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    errorMessage != null && allDealsCount == 0 -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FilledTonalButton(onClick = onRetry) {
                                Text("Retry")
                            }
                        }
                    }
                    filteredCoupons.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp).padding(8.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No courses matching your criteria",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            FilledTonalButton(onClick = onClearFiltersClick) {
                                Text("Reset Filters")
                            }
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 240.dp),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                items = filteredCoupons,
                                key = { it.courseId },
                            ) { item ->
                                CmpCouponCourseCard(
                                    coupon = item,
                                    onClick = {
                                        focusManager.clearFocus()
                                        isSearchFocused = false
                                        onCouponClick(item)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Suggestions Dropdown (exact HomeScreen.kt behavior)
        if (isSearchFocused && suggestions.isNotEmpty()) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 72.dp, top = 68.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    suggestions.forEach { text ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onQueryChange(text)
                                        focusManager.clearFocus()
                                        isSearchFocused = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Exact 1:1 implementation of feature/home/ui/CouponCourseCard.kt
 */
@Composable
internal fun CmpCouponCourseCard(
    coupon: WebCouponSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(2.dp)
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // CouponCardImageHeader (16:9 aspect ratio + CountdownTicker)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.78f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                WebNetworkImage(
                    url = coupon.previewImage,
                    contentDescription = coupon.title ?: "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
                WebCountdownTicker(
                    expiredDate = coupon.expiredTime,
                    fontSize = 10.sp,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp),
                )
            }

            // CouponCardInfoSection
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Text(
                    text = coupon.title ?: "",
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = coupon.author ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = coupon.category ?: "",
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(3.dp))

                // CouponCardRatingBar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatRatingValue(coupon.rating),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RatingAmber,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            val starTint =
                                if (coupon.rating >= i - 0.25f) {
                                    RatingAmber
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = starTint,
                                modifier = Modifier.size(11.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // CouponCardMetricsRow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp),
                ) {
                    Icon(
                        imageVector = CmpIcons.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatStudentsCount(coupon.students),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = CmpIcons.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatReviewsCount(coupon.reviews),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
