package com.cheeke.surfy.model

enum class SearchType(val label: String, val type: String) {
    MULTI(label = "전체", type = "multi"),
    MOVIE(label = "영화", type = "movie"),
    TV(label = "TV", type = "tv"),
    PEOPLE(label = "인물", type = "person"),
    SERIES(label = "시리즈", type = "collection")
}