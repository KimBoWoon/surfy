package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.model.CombineCredits
import com.cheeke.surfy.model.ExternalIds
import com.cheeke.surfy.model.People
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.annotations.VisibleForTesting

class TestPeopleDetailRepository : PeopleDetailRepository {
    private val peopleDetail = MutableSharedFlow<People>(replay = 1, onBufferOverflow = DROP_OLDEST)
    private val combineCredits = MutableSharedFlow<CombineCredits>(replay = 1, onBufferOverflow = DROP_OLDEST)
    private val externalIds = MutableSharedFlow<ExternalIds>(replay = 1, onBufferOverflow = DROP_OLDEST)

    override fun getData(id: Int): Flow<People> = peopleDetail

    override fun getCombineCredits(personId: Int): Flow<CombineCredits> = combineCredits

    override fun getExternalIds(personId: Int): Flow<ExternalIds> = externalIds

    @VisibleForTesting
    fun setPeopleDetail(people: People) {
        peopleDetail.tryEmit(value = people)
    }

    @VisibleForTesting
    fun setCombineCredits(credits: CombineCredits) {
        combineCredits.tryEmit(value = credits)
    }

    @VisibleForTesting
    fun setExternalIds(ids: ExternalIds) {
        externalIds.tryEmit(value = ids)
    }
}