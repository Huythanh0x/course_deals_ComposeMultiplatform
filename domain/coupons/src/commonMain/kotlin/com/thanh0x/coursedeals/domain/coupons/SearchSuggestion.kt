package com.thanh0x.coursedeals.domain.coupons

data class SearchSuggestion(
    val text: String,
    val type: SuggestionType
)

enum class SuggestionType {
    HISTORY,
    KEYWORD
}
