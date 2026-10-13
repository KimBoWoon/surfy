package com.cheeke.surfy.data.repository

import androidx.paging.PagingData
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.TrendingMediaResult
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getTrending(mediaType: MediaType, timeWindow: String, language: String): Flow<PagingData<TrendingMediaResult>>
}