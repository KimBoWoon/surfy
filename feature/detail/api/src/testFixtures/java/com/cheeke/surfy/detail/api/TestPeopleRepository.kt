package com.cheeke.surfy.detail.api

import androidx.paging.PagingData
import com.cheeke.surfy.detail.api.people.PeopleRepository
import com.cheeke.surfy.model.CombineCredits
import com.cheeke.surfy.model.ExternalIds
import com.cheeke.surfy.model.People
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.jetbrains.annotations.VisibleForTesting

class TestPeopleRepository : PeopleRepository {
    val peopleDatabase = MutableSharedFlow<List<People>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val currentPeopleDatabase get() = peopleDatabase.replayCache.firstOrNull().orEmpty()
    private val peopleDetail = MutableSharedFlow<People>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val combineCredits = MutableSharedFlow<CombineCredits>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val externalIds = MutableSharedFlow<ExternalIds>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun getFavorite(): Flow<PagingData<People>> =
        flow { emit(value = PagingData.from(data = currentPeopleDatabase)) }

    override fun isFavorite(id: Int): Flow<Boolean> =
        peopleDatabase
            .map { peoples ->
                peoples.firstOrNull { it.id == id } != null
            }.distinctUntilChanged()

    override suspend fun insert(media: People): Long {
        peopleDatabase.emit(value = currentPeopleDatabase + media)
        return media.id?.toLong() ?: throw RuntimeException("room database insert failed...")
    }

    override suspend fun delete(media: People) {
        peopleDatabase.emit(value = currentPeopleDatabase.filter { it.id != media.id })
    }

    override suspend fun upsert(medias: List<People>) {
        peopleDatabase.emit(
            value = (currentPeopleDatabase + medias).map { people ->
                medias.find { it.id == people.id } ?: people
            }
        )
    }

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