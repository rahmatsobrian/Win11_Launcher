package com.siroha.feature.search

sealed class SearchResultItem {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String?

    data class AppResult(
        override val id: String,
        override val title: String,
        override val subtitle: String? = null,
        val componentKey: String
    ) : SearchResultItem()

    data class SettingResult(
        override val id: String,
        override val title: String,
        override val subtitle: String? = null,
        val settingsRoute: String
    ) : SearchResultItem()

    data class ContactResult(
        override val id: String,
        override val title: String,
        override val subtitle: String? = null,
        val lookupKey: String
    ) : SearchResultItem()

    data class RecentQuery(
        override val id: String,
        override val title: String,
        override val subtitle: String? = null
    ) : SearchResultItem()
}

data class SearchCategoryResults(
    val apps: List<SearchResultItem.AppResult> = emptyList(),
    val settings: List<SearchResultItem.SettingResult> = emptyList(),
    val contacts: List<SearchResultItem.ContactResult> = emptyList()
) {
    val isEmpty: Boolean get() = apps.isEmpty() && settings.isEmpty() && contacts.isEmpty()
}
