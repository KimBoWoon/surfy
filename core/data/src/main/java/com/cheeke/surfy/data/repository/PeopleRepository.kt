package com.cheeke.surfy.data.repository

import androidx.paging.PagingSource
import com.cheeke.surfy.database.dao.PeopleDao
import com.cheeke.surfy.database.model.PeopleEntity
import com.cheeke.surfy.model.CombineCredits
import com.cheeke.surfy.model.ExternalIds
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.People
import com.cheeke.surfy.network.PeopleRemoteDataSource
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import jakarta.inject.Inject
import java.time.Instant

interface PeopleRepository : DataBaseRepository<PeopleEntity>, DetailRepository<People> {
    override fun getData(id: Int): Single<People>
    fun getCombineCredits(personId: Int): Single<CombineCredits>
    fun getExternalIds(personId: Int): Single<ExternalIds>
}

class PeopleRepositoryImpl @Inject constructor(
    private val peopleDao: PeopleDao,
    private val apis: PeopleRemoteDataSource,
    private val requestOptionsProvider: DetailRequestOptionsProvider
) : PeopleRepository {
    override fun getData(id: Int): Single<People> = requestOptionsProvider.current()
        .flatMap {
            apis.getPeopleDetail(
                personId = id,
                language = it.languageTag,
                includeImageLanguage = it.includeImageLanguage
            )
        }

    override fun getCombineCredits(personId: Int): Single<CombineCredits> = requestOptionsProvider.current()
        .flatMap {
            apis.getCombineCredits(
                personId = personId,
                language = it.languageTag
            )
        }

    override fun getExternalIds(personId: Int): Single<ExternalIds> =
        apis.getExternalIds(personId = personId)

    override fun isFavorite(id: Int): Observable<Boolean> = peopleDao.isFavoritePeople(id = id)

    override fun insert(media: Media): Single<Long> =
        peopleDao.insertOrIgnorePeoples(
            PeopleEntity(
                id = media.id ?: -1,
                timestamp = Instant.now().toEpochMilli(),
                name = media.title ?: "",
                profilePath = media.posterPath ?: ""
            )
        ).subscribeOn(Schedulers.io())

    override fun delete(media: Media): Completable {
        return media.id?.let { id ->
            peopleDao.deletePeople(id = id).subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    override fun upsert(medias: List<Media>): Completable =
        peopleDao.upsertPeoples(
            entities = medias.map { media ->
                PeopleEntity(
                    id = media.id ?: -1,
                    timestamp = Instant.now().toEpochMilli(),
                    name = media.title ?: "",
                    profilePath = media.posterPath ?: ""
                )
            }
        ).subscribeOn(Schedulers.io())

    override fun getFavorite(): PagingSource<Int, PeopleEntity> =
        peopleDao.getFavoritePeople()
}