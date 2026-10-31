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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.stringResource
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme
import com.thanh0x.coursedeals.core.ui.theme.RatingAmber
import com.thanh0x.coursedeals.core.ui.util.categoryResource
import com.thanh0x.coursedeals.core.ui.util.languageResource
import com.thanh0x.coursedeals.core.ui.util.sortResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheetDialogContent(
    initialFilter: FilterData,
    onDismissRequest: () -> Unit,
    onApply: (FilterData) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    var selectedCategories by remember(initialFilter) {
        mutableStateOf(initialFilter.categories.toSet())
    }
    var sliderValue by remember(initialFilter) {
        mutableFloatStateOf(ratingToSlider(initialFilter.minRating))
    }
    var selectedLanguage by remember(initialFilter) {
        mutableStateOf(initialFilter.language)
    }
    var selectedSort by remember(initialFilter) {
        mutableStateOf(initialFilter.sortBy)
    }

    val currentRating = sliderToRating(sliderValue)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Text(
                text = stringResource(Res.string.filter_title),
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
                FilterCategorySection(
                    selectedCategories = selectedCategories,
                    onToggleCategory = { category, isSelected ->
                        selectedCategories = if (isSelected) {
                            selectedCategories - category
                        } else {
                            selectedCategories + category
                        }
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))

                FilterRatingSection(
                    currentRating = currentRating,
                    sliderValue = sliderValue,
                    onSliderChange = { sliderValue = it },
                )

                Spacer(modifier = Modifier.height(20.dp))

                FilterLanguageSection(
                    selectedLanguage = selectedLanguage,
                    onSelectLanguage = { selectedLanguage = it },
                )

                Spacer(modifier = Modifier.height(20.dp))

                FilterSortSection(
                    selectedSort = selectedSort,
                    onSelectSort = { selectedSort = it },
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            FilterActionFooter(
                onReset = {
                    selectedCategories = emptySet()
                    selectedLanguage = CourseLanguage.ALL
                    selectedSort = SortOption.NEWEST
                    sliderValue = 0f
                },
                onApply = {
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
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterCategorySection(
    selectedCategories: Set<CourseCategory>,
    onToggleCategory: (CourseCategory, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.fav_cats_title),
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
                    onClick = { onToggleCategory(category, isSelected) },
                    label = { Text(stringResource(category.categoryResource)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }
    }
}

@Composable
private fun FilterRatingSection(
    currentRating: Double,
    sliderValue: Float,
    onSliderChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.filter_rating),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = if (currentRating > 0.0) {
                    stringResource(Res.string.filter_rating_format, currentRating)
                } else {
                    stringResource(Res.string.filter_rating_any)
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = RatingAmber,
            )
        }

        Slider(
            value = sliderValue,
            onValueChange = onSliderChange,
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
                text = stringResource(Res.string.filter_rating_any),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Text(
                text = stringResource(Res.string.filter_rating_max),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterLanguageSection(
    selectedLanguage: CourseLanguage,
    onSelectLanguage: (CourseLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.lang_label),
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
                    onClick = { onSelectLanguage(language) },
                    label = { Text(stringResource(language.languageResource)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSortSection(
    selectedSort: SortOption,
    onSelectSort: (SortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.sort_label),
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
                    onClick = { onSelectSort(sort) },
                    label = { Text(stringResource(sort.sortResource)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }
    }
}

@Composable
private fun FilterActionFooter(
    onReset: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        TextButton(
            onClick = onReset,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(Res.string.filter_reset))
        }

        Button(
            onClick = onApply,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) {
            Text(stringResource(Res.string.filter_apply))
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
