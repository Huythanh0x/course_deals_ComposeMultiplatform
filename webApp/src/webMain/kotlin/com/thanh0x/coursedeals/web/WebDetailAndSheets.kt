package com.thanh0x.coursedeals.web

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

internal val allCmpCategories =
    listOf(
        "Development",
        "Design",
        "Business",
        "IT & Software",
        "Marketing",
        "Personal Development",
        "Photography",
        "Music",
        "Health & Fitness",
        "Finance",
    )

internal enum class WebCourseLanguage(val label: String) {
    ALL("All"),
    ENGLISH("English"),
    OTHERS("Others"),
}

internal enum class WebSortOption(val label: String) {
    NEWEST("Newest"),
    RATING("Rating"),
    STUDENTS("Students"),
    REVIEWS("Reviews"),
    EXPIRING_SOON("Expiring Soon"),
}

internal data class WebFilterData(
    val categories: Set<String> = emptySet(),
    val language: WebCourseLanguage = WebCourseLanguage.ALL,
    val sortBy: WebSortOption = WebSortOption.NEWEST,
    val minRating: Double = 0.0,
) {
    val isActive: Boolean
        get() =
            categories.isNotEmpty() ||
                language != WebCourseLanguage.ALL ||
                sortBy != WebSortOption.NEWEST ||
                minRating > 0.0
}

@Serializable
data class WebCouponDetail(
    val courseId: Int = 0,
    val title: String? = null,
    val category: String? = null,
    val subCategory: String? = null,
    val level: String? = null,
    val language: String? = null,
    val author: String? = null,
    val rating: Float = 0f,
    val reviews: Int = 0,
    val students: Int = 0,
    val contentLength: Int? = null,
    val usesRemaining: Int? = null,
    val couponCode: String? = null,
    val couponUrl: String? = null,
    val heading: String? = null,
    val description: String? = null,
    val previewImage: String? = null,
    val expiredTime: Long? = null,
    val isNew: Boolean = false,
)

private const val MIN_QUALIFIED_RATING = 4.0
private const val RATING_STEP_FACTOR = 10.0
private const val MAX_SLIDER_STEPS = 10
private const val MAX_SLIDER_VALUE = 11.0f

private fun sliderToRating(value: Float): Double =
    if (value == 0.0f) 0.0 else MIN_QUALIFIED_RATING + ((value - 1.0) / RATING_STEP_FACTOR)

private fun ratingToSlider(rating: Double): Float =
    if (rating <= 0.0) {
        0.0f
    } else {
        (((rating - MIN_QUALIFIED_RATING) * RATING_STEP_FACTOR) + 1.0)
            .toFloat()
            .coerceIn(0.0f, MAX_SLIDER_VALUE)
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun WebFilterBottomSheet(
    initialFilter: WebFilterData,
    onDismissRequest: () -> Unit,
    onApply: (WebFilterData) -> Unit,
) {
    var selectedCategories by remember(initialFilter) { mutableStateOf(initialFilter.categories) }
    var sliderValue by remember(initialFilter) { mutableFloatStateOf(ratingToSlider(initialFilter.minRating)) }
    var selectedLanguage by remember(initialFilter) { mutableStateOf(initialFilter.language) }
    var selectedSort by remember(initialFilter) { mutableStateOf(initialFilter.sortBy) }
    val currentRating = sliderToRating(sliderValue)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text(
                text = "Filter",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "Favorite Categories",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    allCmpCategories.forEach { category ->
                        val isSelected = selectedCategories.contains(category)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategories =
                                    if (isSelected) selectedCategories - category else selectedCategories + category
                            },
                            label = { Text(category) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Minimum Rating",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text =
                            if (currentRating > 0.0) {
                                "${formatRatingValue(currentRating.toFloat())}+ Stars"
                            } else {
                                "Any Rating"
                            },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = RatingAmber,
                    )
                }
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..11f,
                    steps = MAX_SLIDER_STEPS,
                    colors =
                        SliderDefaults.colors(
                            thumbColor = RatingAmber,
                            activeTrackColor = RatingAmber,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Any Rating",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                    Text(
                        text = "5.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Language",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    WebCourseLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = selectedLanguage == lang,
                            onClick = { selectedLanguage = lang },
                            label = { Text(lang.label) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    WebSortOption.entries.forEach { sort ->
                        FilterChip(
                            selected = selectedSort == sort,
                            onClick = { selectedSort = sort },
                            label = { Text(sort.label) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                TextButton(
                    onClick = {
                        selectedCategories = emptySet()
                        selectedLanguage = WebCourseLanguage.ALL
                        selectedSort = WebSortOption.NEWEST
                        sliderValue = 0f
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = {
                        onApply(
                            WebFilterData(
                                categories = selectedCategories,
                                language = selectedLanguage,
                                sortBy = selectedSort,
                                minRating = currentRating,
                            ),
                        )
                        onDismissRequest()
                    },
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebSubmitDealBottomSheet(
    onDismissRequest: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var urlText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun submit() {
        val trimmed = urlText.trim()
        if (trimmed.isEmpty() || !trimmed.contains("udemy.com")) {
            errorMessage = "Please enter a valid Udemy URL"
        } else {
            errorMessage = null
            onSubmit(trimmed)
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(
                text = "Submit a Deal",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "Enter the Udemy course URL with the coupon code included.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        if (errorMessage != null) errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Course URL") },
                    placeholder = { Text("https://www.udemy.com/course/...") },
                    leadingIcon = {
                        Icon(
                            imageVector = CmpIcons.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { msg -> { Text(msg) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { submit() },
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                ) {
                    Text("Submit")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun WebCouponDetailScreen(
    detail: WebCouponDetail?,
    isLoading: Boolean,
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onShareClick: (title: String, url: String) -> Unit,
    onEnrollClick: (String) -> Unit,
    onReportSubmitted: (reason: String, otherDetails: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showReportSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (detail != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Row(
                            modifier =
                                Modifier
                                    .widthIn(max = 960.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = { showReportSheet = true },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.height(48.dp),
                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error,
                                    ),
                            ) {
                                Icon(
                                    imageVector = CmpIcons.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Report",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    val targetUrl =
                                        detail.couponUrl?.takeIf { it.isNotBlank() }
                                            ?: "https://www.udemy.com/course/${detail.courseId}/"
                                    onEnrollClick(targetUrl)
                                },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.weight(1f).height(48.dp),
                            ) {
                                Text(
                                    text = "Enroll Now",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        if (detail == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = 960.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                ) {
                    DetailHeroHeaderSection(
                        detail = detail,
                        isBookmarked = isBookmarked,
                        onBackClick = onBackClick,
                        onBookmarkToggle = onBookmarkToggle,
                        onShareClick = {
                            val shareUrl =
                                detail.couponUrl?.takeIf { it.isNotBlank() }
                                    ?: "https://coupons.thanh0x.com/courses/${detail.courseId}"
                            onShareClick(detail.title ?: "Course Deal", shareUrl)
                            scope.launch {
                                snackbarHostState.showSnackbar("Course link copied to clipboard!")
                            }
                        },
                    )
                    DetailBodySections(detail = detail)
                }
            }
        }
    }

    if (showReportSheet) {
        WebReportDealBottomSheet(
            onDismissRequest = { showReportSheet = false },
            onSubmitReport = { reason, otherDetails ->
                showReportSheet = false
                onReportSubmitted(reason, otherDetails)
                scope.launch {
                    snackbarHostState.showSnackbar("Deal reported. Thanks!")
                }
            },
        )
    }
}

@Composable
private fun DetailHeroHeaderSection(
    detail: WebCouponDetail,
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onShareClick: () -> Unit,
) {
    val buttonColors =
        IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .aspectRatio(1.78f)
                .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        WebNetworkImage(
            url = detail.previewImage,
            contentDescription = detail.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
        ) {
            IconButton(
                onClick = onBackClick,
                colors = buttonColors,
                modifier = Modifier.align(Alignment.CenterStart).clip(CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = onBookmarkToggle,
                    colors = buttonColors,
                    modifier = Modifier.clip(CircleShape),
                ) {
                    Icon(
                        imageVector = if (isBookmarked) CmpIcons.Bookmark else CmpIcons.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint =
                            if (isBookmarked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                }
                IconButton(
                    onClick = onShareClick,
                    colors = buttonColors,
                    modifier = Modifier.clip(CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                    )
                }
            }
        }
        WebCountdownTicker(
            expiredDate = detail.expiredTime,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailBodySections(detail: WebCouponDetail) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // CourseTitleSection
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        detail.category?.takeIf { it.isNotBlank() }?.let { cat ->
            Text(
                text = cat,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = detail.title ?: "",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        detail.author?.takeIf { it.isNotBlank() }?.let { author ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            detail.language?.takeIf { it.isNotBlank() }?.let { lang ->
                AssistChip(
                    onClick = {},
                    label = { Text(text = lang, fontSize = 11.sp) },
                    colors =
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    border = null,
                )
            }
            detail.level?.takeIf { it.isNotBlank() }?.let { level ->
                AssistChip(
                    onClick = {},
                    label = { Text(text = level, fontSize = 11.sp) },
                    colors =
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    border = null,
                )
            }
        }
    }

    // CourseRatingAndMetricsSection
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatRatingValue(detail.rating),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RatingAmber,
            )
            Spacer(modifier = Modifier.width(6.dp))
            for (i in 1..5) {
                val starTint = if (detail.rating >= i - 0.25f) RatingAmber else MaterialTheme.colorScheme.outlineVariant
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = starTint,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DetailMetricCard(
                icon = CmpIcons.Group,
                value = formatStudentsCount(detail.students),
                label = "Students",
                modifier = Modifier.weight(1f),
            )
            DetailMetricCard(
                icon = CmpIcons.ChatBubbleOutline,
                value = formatReviewsCount(detail.reviews),
                label = "Reviews",
                modifier = Modifier.weight(1f),
            )
            detail.contentLength?.let { length ->
                DetailMetricCard(
                    icon = CmpIcons.Schedule,
                    value = formatContentLength(length),
                    label = "Duration",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    // CouponPricingSection
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SuggestionChip(
                onClick = {},
                label = { Text("100% OFF", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                colors =
                    SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                border = null,
            )
            SuggestionChip(
                onClick = {},
                label = { Text("FREE", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                colors =
                    SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                border = null,
            )
            detail.usesRemaining?.takeIf { it > 0 }?.let { remaining ->
                SuggestionChip(
                    onClick = {},
                    label = { Text("$remaining left", fontSize = 12.sp) },
                    border = null,
                )
            }
        }
        detail.couponCode?.takeIf { it.isNotBlank() }?.let { code ->
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(code))
                    copied = true
                    scope.launch {
                        delay(2000)
                        copied = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    imageVector = if (copied) Icons.Default.Check else CmpIcons.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (copied) "Coupon Copied: $code" else "Coupon: $code (Tap to copy)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    // CourseDescriptionSection
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        detail.heading?.takeIf { it.isNotBlank() }?.let { heading ->
            Text(
                text = "HEADING",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = heading,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        val plainDesc = stripHtmlToPlainText(detail.description)
        if (plainDesc.isNotBlank()) {
            Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                Text(
                    text = "DESCRIPTION",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plainDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 6,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isExpanded) "Show less" else "Read more",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { isExpanded = !isExpanded },
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailMetricCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val reportReasons =
    listOf(
        "Not free",
        "No data",
        "Expired deal",
        "Other",
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebReportDealBottomSheet(
    onDismissRequest: () -> Unit,
    onSubmitReport: (reason: String, otherDetails: String?) -> Unit,
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var otherReasonText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Report this deal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
                reportReasons.forEach { reason ->
                    val selected = selectedReason == reason
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selected,
                                    onClick = {
                                        selectedReason = reason
                                        isError = false
                                    },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            if (selectedReason == "Other") {
                OutlinedTextField(
                    value = otherReasonText,
                    onValueChange = {
                        otherReasonText = it
                        if (isError && it.isNotBlank()) isError = false
                    },
                    placeholder = { Text("Please describe the issue") },
                    isError = isError,
                    supportingText = if (isError) ({ Text("Please describe the issue") }) else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        val current = selectedReason ?: return@Button
                        if (current == "Other" && otherReasonText.trim().isEmpty()) {
                            isError = true
                            return@Button
                        }
                        onSubmitReport(current, if (current == "Other") otherReasonText.trim() else null)
                    },
                    enabled = selectedReason != null,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Submit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
