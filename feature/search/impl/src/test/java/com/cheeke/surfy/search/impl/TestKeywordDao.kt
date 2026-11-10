package com.cheeke.surfy.search.impl

import androidx.paging.PagingSource
import androidx.paging.testing.asPagingSourceFactory
import com.cheeke.surfy.database.impl.dao.KeywordDao
import com.cheeke.surfy.database.impl.model.KeywordEntity
import com.cheeke.surfy.testing.model.keywordList
import kotlinx.coroutines.flow.MutableStateFlow

class TestKeywordDao : KeywordDao {
    val entitiesStateFlow = MutableStateFlow(value = emptyList<KeywordEntity>())

    override fun getKeywords(): PagingSource<Int, KeywordEntity> =
        keywordList.asPagingSourceFactory().invoke()

    override suspend fun insertOrReplaceKeyword(entity: KeywordEntity): Long {
        entitiesStateFlow.emit(value = entitiesStateFlow.value + entity)
        return entity.keyword.hashCode().toLong()
    }

    override suspend fun deleteAll() {
        entitiesStateFlow.emit(value = emptyList())
    }

    override suspend fun delete(keyword: String) {
        entitiesStateFlow.emit(value = entitiesStateFlow.value.filterNot { it.keyword == keyword })
    }
}