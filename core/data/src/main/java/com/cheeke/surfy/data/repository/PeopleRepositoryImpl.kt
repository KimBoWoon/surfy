package com.cheeke.surfy.data.repository

import androidx.paging.PagingSource
import com.cheeke.surfy.database.dao.PeopleDao
import com.cheeke.surfy.database.model.PeopleEntity
import com.cheeke.surfy.model.CombineCredits
import com.cheeke.surfy.model.ExternalIds
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.People
import com.cheeke.surfy.network.PeopleRemoteDataSource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant

class PeopleRepositoryImpl @Inject constructor(
    private val peopleDao: PeopleDao,
    private val apis: PeopleRemoteDataSource,
    private val requestOptionsProvider: DetailRequestOptionsProvider
) : PeopleRepository {
    override fun isFavorite(id: Int): Flow<Boolean> = peopleDao.isFavoritePeople(id = id)

    override suspend fun insert(media: Media): Long =
        peopleDao.insertOrIgnorePeoples(
            PeopleEntity(
                id = media.id ?: -1,
                timestamp = Instant.now().toEpochMilli(),
                name = media.title ?: "",
                profilePath = media.posterPath ?: ""
            )
        )

    override suspend fun delete(media: Media) {
        media.id?.let { id ->
            peopleDao.deletePeople(id = id)
        }
    }

    override suspend fun upsert(medias: List<Media>) =
        peopleDao.upsertPeoples(
            entities = medias.map { media ->
                PeopleEntity(
                    id = media.id ?: -1,
                    timestamp = Instant.now().toEpochMilli(),
                    name = media.title ?: "",
                    profilePath = media.posterPath ?: ""
                )
            }
        )

    override fun getFavorite(): PagingSource<Int, PeopleEntity> =
        peopleDao.getFavoritePeople()

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
}