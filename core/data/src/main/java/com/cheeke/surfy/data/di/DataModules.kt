package com.cheeke.surfy.data.di

import com.cheeke.surfy.data.repository.HomeRepository
import com.cheeke.surfy.data.repository.HomeRepositoryImpl
import com.cheeke.surfy.data.repository.MovieRepository
import com.cheeke.surfy.data.repository.MovieRepositoryImpl
import com.cheeke.surfy.data.repository.PeopleRepository
import com.cheeke.surfy.data.repository.PeopleRepositoryImpl
import com.cheeke.surfy.data.repository.SearchRepository
import com.cheeke.surfy.data.repository.SearchRepositoryImpl
import com.cheeke.surfy.data.repository.SeriesRepository
import com.cheeke.surfy.data.repository.SeriesRepositoryImpl
import com.cheeke.surfy.data.repository.SyncRepository
import com.cheeke.surfy.data.repository.SyncRepositoryImpl
import com.cheeke.surfy.data.repository.TvRepository
import com.cheeke.surfy.data.repository.TvRepositoryImpl
import com.cheeke.surfy.data.repository.UserDataRepository
import com.cheeke.surfy.data.repository.UserDataRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModules {
    @Binds
    abstract fun bindUserRepository(
        userDataRepository: UserDataRepositoryImpl
    ): UserDataRepository

    @Binds
    abstract fun bindMovieRepository(
        movieDetailRepository: MovieRepositoryImpl
    ): MovieRepository

    @Binds
    abstract fun bindPeopleRepository(
        peopleDetailRepository: PeopleRepositoryImpl
    ): PeopleRepository

    @Binds
    abstract fun bindTvRepository(
        tvDetailRepository: TvRepositoryImpl
    ): TvRepository

    @Binds
    abstract fun bindSeriesRepository(
        seriesDetailRepository: SeriesRepositoryImpl
    ): SeriesRepository

    @Binds
    abstract fun bindSearchRepository(
        databaseRepository: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    abstract fun bindMainMenuRepository(
        mainMenuRepository: SyncRepositoryImpl
    ): SyncRepository

    @Binds
    abstract fun bindPagingRepository(
        pagingRepository: HomeRepositoryImpl
    ): HomeRepository
}