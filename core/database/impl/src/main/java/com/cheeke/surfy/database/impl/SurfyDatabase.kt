package com.cheeke.surfy.database.impl

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cheeke.surfy.database.impl.dao.KeywordDao
import com.cheeke.surfy.database.impl.dao.MovieDao
import com.cheeke.surfy.database.impl.dao.PeopleDao
import com.cheeke.surfy.database.impl.dao.TvDao
import com.cheeke.surfy.database.impl.model.KeywordEntity
import com.cheeke.surfy.database.impl.model.MovieEntity
import com.cheeke.surfy.database.impl.model.NowPlayingMovieEntity
import com.cheeke.surfy.database.impl.model.PeopleEntity
import com.cheeke.surfy.database.impl.model.TvEntity
import com.cheeke.surfy.database.impl.model.UpComingMovieEntity

@Database(
    entities = [
        MovieEntity::class,
        TvEntity::class,
        PeopleEntity::class,
        NowPlayingMovieEntity::class,
        UpComingMovieEntity::class,
        KeywordEntity::class
    ],
    version = 2,
    autoMigrations = [],
    exportSchema = true
)
internal abstract class SurfyDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun peopleDao(): PeopleDao
    abstract fun tvDao(): TvDao
    abstract fun keywordDao(): KeywordDao
}