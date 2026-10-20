package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.detail.api.movie.MovieRepository
import com.cheeke.surfy.detail.api.people.PeopleRepository
import com.cheeke.surfy.detail.api.series.SeriesRepository
import com.cheeke.surfy.detail.api.tv.TvRepository
import com.cheeke.surfy.detail.impl.movie.MovieRepositoryImpl
import com.cheeke.surfy.detail.impl.people.PeopleRepositoryImpl
import com.cheeke.surfy.detail.impl.series.SeriesRepositoryImpl
import com.cheeke.surfy.detail.impl.tv.TvRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DetailRepositoryModules {
    @Binds
    abstract fun bindSeriesRepository(
        seriesDetailRepository: SeriesRepositoryImpl
    ): SeriesRepository

    @Binds
    abstract fun bindMovieRepository(
        movieRepository: MovieRepositoryImpl
    ): MovieRepository

    @Binds
    abstract fun bindPeopleRepository(
        movieRepository: PeopleRepositoryImpl
    ): PeopleRepository

    @Binds
    abstract fun bindTvRepository(
        movieRepository: TvRepositoryImpl
    ): TvRepository
}