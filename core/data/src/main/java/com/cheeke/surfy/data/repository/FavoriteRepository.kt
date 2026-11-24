package com.cheeke.surfy.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.rxjava3.flowable
import com.cheeke.surfy.database.model.MovieEntity
import com.cheeke.surfy.database.model.PeopleEntity
import com.cheeke.surfy.database.model.TvEntity
import com.cheeke.surfy.database.model.asExternalModel
import com.cheeke.surfy.model.Media
import io.reactivex.rxjava3.core.Flowable
import jakarta.inject.Inject

enum class FavoriteKeys {
    MOVIE, TV, PEOPLE
}

sealed interface FavoriteRepository {
    val key: FavoriteKeys
    val pagingSource: Flowable<PagingData<Media>>

    fun insert(media: Media)
    fun delete(media: Media)
}

class FavoriteMovieRepository @Inject constructor(
    private val repository: MovieRepository
) : FavoriteRepository {
    override val key: FavoriteKeys = FavoriteKeys.MOVIE
    override val pagingSource: Flowable<PagingData<Media>> =
        Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = { repository.getFavorite() }
        ).flowable.map { pagingData ->
            pagingData.map(transform = MovieEntity::asExternalModel)
        }

    override fun insert(media: Media) {
        repository.insert(media = media).subscribe()
    }

    override fun delete(media: Media) {
        repository.delete(media = media).subscribe()
    }
}

class FavoritePeopleRepository @Inject constructor(
    private val repository: PeopleRepository
) : FavoriteRepository {
    override val key: FavoriteKeys = FavoriteKeys.PEOPLE
    override val pagingSource: Flowable<PagingData<Media>> =
        Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = { repository.getFavorite() }
        ).flowable.map { pagingData ->
            pagingData.map(transform = PeopleEntity::asExternalModel)
        }

    override fun insert(media: Media) {
        repository.insert(media = media).subscribe()
    }

    override fun delete(media: Media) {
        repository.delete(media = media).subscribe()
    }
}

class FavoriteTvRepository @Inject constructor(
    private val repository: TvRepository
) : FavoriteRepository {
    override val key: FavoriteKeys = FavoriteKeys.TV
    override val pagingSource: Flowable<PagingData<Media>> =
        Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = { repository.getFavorite() }
        ).flowable.map { pagingData ->
            pagingData.map(transform = TvEntity::asExternalModel)
        }

    override fun insert(media: Media) {
        repository.insert(media = media).subscribe()
    }

    override fun delete(media: Media) {
        repository.delete(media = media).subscribe()
    }
}