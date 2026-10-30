package com.thanh0x.coursedeals.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanh0x.coursedeals.domain.coupons.SearchSuggestion
import com.thanh0x.coursedeals.domain.coupons.SuggestionType

@Composable
fun SearchSuggestionsDropdown(
    suggestions: List<SearchSuggestion>,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (suggestions.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 260.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(suggestions) { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val icon = when (suggestion.type) {
                        SuggestionType.HISTORY -> Icons.Default.History
                        SuggestionType.KEYWORD -> Icons.Default.Search
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = suggestion.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp,
                )
            }
        }
    }
}
