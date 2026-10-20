package com.cheeke.surfy.model

enum class SearchType(val label: String, val path: String) {
    MULTI(label = "전체", path = "multi"),
    MOVIE(label = "영화", path = "movie"),
    TV(label = "TV", path = "tv"),
    PEOPLE(label = "인물", path = "person"),
    SERIES(label = "시리즈", path = "collection")
}