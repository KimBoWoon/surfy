package com.cheeke.surfy.detail.impl.people

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cheeke.surfy.database.impl.dao.PeopleDao
import com.cheeke.surfy.database.impl.model.PeopleEntity
import com.cheeke.surfy.database.impl.model.asExternalModel
import com.cheeke.surfy.detail.api.people.PeopleRepository
import com.cheeke.surfy.detail.impl.DetailRequestOptionsProvider
import com.cheeke.surfy.model.CombineCredits
import com.cheeke.surfy.model.ExternalIds
import com.cheeke.surfy.model.People
import com.cheeke.surfy.network.api.PeopleRemoteDataSource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class PeopleRepositoryImpl @Inject constructor(
    private val peopleDao: PeopleDao,
    private val apis: PeopleRemoteDataSource,
    private val requestOptionsProvider: DetailRequestOptionsProvider
) : PeopleRepository {
    override fun getData(id: Int): Flow<People> = flow {
        val internalData = requestOptionsProvider.current()

        emit(value = apis.getPeopleDetail(personId = id, language = internalData.languageTag, includeImageLanguage = internalData.includeImageLanguage))
    }

    override fun getCombineCredits(personId: Int): Flow<CombineCredits> = flow {
        val internalData = requestOptionsProvider.current()

        emit(value = apis.getCombineCredits(personId = personId, language = internalData.languageTag))
    }

    override fun getExternalIds(personId: Int): Flow<ExternalIds> = flow {
        emit(value = apis.getExternalIds(personId = personId))
    }

    override fun getFavorite(): Flow<PagingData<People>> =
        Pager(config = PagingConfig(pageSize = 20)) {
            peopleDao.getFavoritePeople()
        }.flow.map { pagingData -> pagingData.map(transform = PeopleEntity::asExternalModel) }

    override fun isFavorite(id: Int): Flow<Boolean> =
        peopleDao.isFavoritePeople(id = id)

    override suspend fun insert(media: People): Long =
        peopleDao.insertOrIgnorePeoples(
            people = PeopleEntity(
                id = media.id ?: -1,
                name = media.title,
                profilePath = media.posterPath ?: "",
                timestamp = Instant.now().toEpochMilli()
            )
        )

    override suspend fun delete(media: People) {
        media.id?.let { id ->
            peopleDao.deletePeople(id = id)
        }
    }

    override suspend fun upsert(medias: List<People>) {
        peopleDao.upsertPeoples(entities = medias.map(transform = People::asExternalModel))
    }
}