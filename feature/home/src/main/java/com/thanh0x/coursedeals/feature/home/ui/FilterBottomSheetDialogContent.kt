package com.thanh0x.coursedeals.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.ui.R as CoreR
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals.core.ui.theme.RatingAmber
import com.thanh0x.coursedeals.core.ui.util.categoryResId
import com.thanh0x.coursedeals.core.ui.util.languageResId
import com.thanh0x.coursedeals.core.ui.util.sortResId
import com.thanh0x.coursedeals.domain.coupons.CourseCategory
import com.thanh0x.coursedeals.domain.coupons.CourseLanguage
import com.thanh0x.coursedeals.domain.coupons.FilterData
import com.thanh0x.coursedeals.domain.coupons.SortOption

private const val MIN_QUALIFIED_RATING = 4.0
private const val RATING_STEP_FACTOR = 10.0
private const val MAX_SLIDER_STEPS = 10 // 0 to 11 has 10 steps

private fun sliderToRating(value: Float): Double {
    return if (value == 0.0f) {
        0.0
    } else {
        MIN_QUALIFIED_RATING + ((value - 1.0) / RATING_STEP_FACTOR)
    }
}

private fun ratingToSlider(rating: Double): Float {
    return if (rating <= 0.0) {
        0.0f
    } else {
        (((rating - MIN_QUALIFIED_RATING) * RATING_STEP_FACTOR) + 1.0)
            .toFloat()
            .coerceIn(0.0f, 11.0f)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheetDialogContent(
    initialFilter: FilterData,
    onDismissRequest: () -> Unit,
    onApply: (FilterData) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    var selectedCategories by remember { mutableStateOf(initialFilter.categories.toSet()) }
    var selectedLanguage by remember { mutableStateOf(initialFilter.language) }
    var selectedSort by remember { mutableStateOf(initialFilter.sortBy) }
    var sliderValue by remember { mutableFloatStateOf(ratingToSlider(initialFilter.minRating)) }

    val currentRating = sliderToRating(sliderValue)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Text(
                text = stringResource(CoreR.string.filter_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                // Category Section
                Text(
                    text = stringResource(CoreR.string.fav_cats_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CourseCategory.entries.forEach { category ->
                        val isSelected = selectedCategories.contains(category)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategories = if (isSelected) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories + category
                                }
                            },
                            label = { Text(stringResource(category.categoryResId)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Rating Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(CoreR.string.filter_rating),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = if (currentRating > 0.0) {
                            stringResource(CoreR.string.filter_rating_format, currentRating)
                        } else {
                            stringResource(CoreR.string.filter_rating_any)
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
                    colors = SliderDefaults.colors(
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
                        text = stringResource(CoreR.string.filter_rating_any),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                    Text(
                        text = stringResource(CoreR.string.filter_rating_max),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Language Section
                Text(
                    text = stringResource(CoreR.string.lang_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CourseLanguage.entries.forEach { language ->
                        val isSelected = selectedLanguage == language
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLanguage = language },
                            label = { Text(stringResource(language.languageResId)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sort Section
                Text(
                    text = stringResource(CoreR.string.sort_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SortOption.entries.forEach { sort ->
                        val isSelected = selectedSort == sort
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSort = sort },
                            label = { Text(stringResource(sort.sortResId)) },
                            colors = FilterChipDefaults.filterChipColors(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                TextButton(
                    onClick = {
                        selectedCategories = emptySet()
                        selectedLanguage = CourseLanguage.ALL
                        selectedSort = SortOption.NEWEST
                        sliderValue = 0f
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(CoreR.string.filter_reset))
                }

                Button(
                    onClick = {
                        onApply(
                            FilterData(
                                categories = selectedCategories.toList(),
                                language = selectedLanguage,
                                sortBy = selectedSort,
                                minRating = currentRating,
                            ),
                        )
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                ) {
                    Text(stringResource(CoreR.string.filter_apply))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun FilterBottomSheetDialogPreview() {
    CourseDealsTheme {
        FilterBottomSheetDialogContent(
            initialFilter = FilterData(),
            onDismissRequest = {},
            onApply = {},
        )
    }
}
