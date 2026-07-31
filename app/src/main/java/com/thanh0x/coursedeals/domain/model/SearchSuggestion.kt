package com.thanh0x.coursedeals.domain.model

data class SearchSuggestion(
    val text: String,
    val type: SuggestionType,
)

enum class SuggestionType {
    HISTORY,
    KEYWORD
}
