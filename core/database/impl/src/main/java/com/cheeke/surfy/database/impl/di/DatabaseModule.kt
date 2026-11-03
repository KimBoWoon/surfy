package com.cheeke.surfy.database.impl.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cheeke.surfy.database.impl.SurfyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesMovieDatabase(
        @ApplicationContext context: Context
    ): SurfyDatabase = Room.databaseBuilder(
        context = context,
        klass = SurfyDatabase::class.java,
        name = "surfy-database.db"
    ).addMigrations(MIGRATION_1_2)/*.addCallback(
        callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                context.assets.open("surfy-database_db-movies.sql").use { inputStream ->
                    val sql = inputStream.bufferedReader().use { it.readText() }

                    sql.split(";")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && it.startsWith(prefix = "INSERT") }
                        .forEach { statement ->
                            runCatching {
                                db.execSQL(sql = statement)
                            }.onFailure {
                                Log.e(statement)
                            }
                        }
                }

                context.assets.open("surfy-database_db-peoples.sql").use { inputStream ->
                    val sql = inputStream.bufferedReader().use { it.readText() }

                    sql.split(";")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && it.startsWith(prefix = "INSERT") }
                        .forEach { statement ->
                            runCatching {
                                db.execSQL(sql = statement)
                            }.onFailure {
                                Log.e(statement)
                            }
                        }
                }

                context.assets.open("surfy-database_db-tvs.sql").use { inputStream ->
                    val sql = inputStream.bufferedReader().use { it.readText() }

                    sql.split(";")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && it.startsWith(prefix = "INSERT") }
                        .forEach { statement ->
                            runCatching {
                                db.execSQL(sql = statement)
                            }.onFailure {
                                Log.e(statement)
                            }
                        }
                }
            }
        }
    )*/.build()

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // 1. 새로운 테이블 생성
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS recentlyKeyword_new (
                keyword TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                PRIMARY KEY(keyword)
            )
            """.trimIndent()
            )

            // 2. 기존 데이터 복사 (중복 키워드는 최신 timestamp 유지)
            db.execSQL(
                """
            INSERT INTO recentlyKeyword_new (keyword, timestamp)
            SELECT keyword, MAX(timestamp)
            FROM recentlyKeyword
            GROUP BY keyword
            """.trimIndent()
            )

            // 3. 기존 테이블 삭제
            db.execSQL("DROP TABLE recentlyKeyword")

            // 4. 새로운 테이블 이름 변경
            db.execSQL(
                "ALTER TABLE recentlyKeyword_new RENAME TO recentlyKeyword"
            )
        }
    }
}