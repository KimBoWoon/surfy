package com.cheeke.surfy.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cheeke.surfy.database.model.MovieEntity
import com.cheeke.surfy.database.model.PeopleEntity
import com.cheeke.surfy.database.model.TvEntity
import com.cheeke.surfy.database.model.asExternalModel
import com.cheeke.surfy.model.Media
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class FavoriteKeys {
    MOVIE, TV, PEOPLE
}

sealed interface FavoriteRepository {
    val key: FavoriteKeys
    val pagingSource: Flow<PagingData<Media>>

    suspend fun insert(media: Media)
    suspend fun delete(media: Media)
}

class FavoriteMovieRepository @Inject constructor(
    private val repository: MovieRepository
) : FavoriteRepository {
    override val key: FavoriteKeys = FavoriteKeys.MOVIE
    override val pagingSource: Flow<PagingData<Media>> =
        Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = { repository.getFavorite() }
        ).flow.map { pagingData ->
            pagingData.map(transform = MovieEntity::asExternalModel)
        }

    override suspend fun insert(media: Media) {
        repository.insert(media = media)
    }

    override suspend fun delete(media: Media) {
        repository.delete(media = media)
    }
}

class FavoritePeopleRepository @Inject constructor(
    private val repository: PeopleRepository
) : FavoriteRepository {
    override val key: FavoriteKeys = FavoriteKeys.PEOPLE
    override val pagingSource: Flow<PagingData<Media>> =
        Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = { repository.getFavorite() }
        ).flow.map { pagingData ->
            pagingData.map(transform = PeopleEntity::asExternalModel)
        }

    override suspend fun insert(media: Media) {
        repository.insert(media = media)
    }

    override suspend fun delete(media: Media) {
        repository.delete(media = media)
    }
}

class FavoriteTvRepository @Inject constructor(
    private val repository: TvRepository
) : FavoriteRepository {
    override val key: FavoriteKeys = FavoriteKeys.TV
    override val pagingSource: Flow<PagingData<Media>> =
        Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = { repository.getFavorite() }
        ).flow.map { pagingData ->
            pagingData.map(transform = TvEntity::asExternalModel)
        }

    override suspend fun insert(media: Media) {
        repository.insert(media = media)
    }

    override suspend fun delete(media: Media) {
        repository.delete(media = media)
    }
}