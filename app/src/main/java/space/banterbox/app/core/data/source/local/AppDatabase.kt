package space.banterbox.app.core.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import space.banterbox.app.core.data.source.local.dao.UploadFilesDao
import space.banterbox.app.core.data.source.local.dao.UploadSessionDao
import space.banterbox.app.core.data.source.local.entity.UploadFilesEntity
import space.banterbox.app.core.data.source.local.entity.UploadSessionEntity
import space.banterbox.app.core.data.source.local.dao.CacheKeysDao
import space.banterbox.app.core.data.source.local.dao.NotificationDao
import space.banterbox.app.core.data.source.local.entity.CacheKeysEntity
import space.banterbox.app.core.data.source.local.entity.NotificationEntity

@Database(
    entities = [UploadSessionEntity::class, UploadFilesEntity::class,
        CacheKeysEntity::class, NotificationEntity::class],
    version = 1,
    /*autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],*/
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun uploadSessionDao(): UploadSessionDao

    abstract fun uploadFilesDao(): UploadFilesDao

    abstract fun cacheKeysDao(): CacheKeysDao

    abstract fun notificationDao(): NotificationDao

    class Factory {
        fun createInstance(appContext: Context): AppDatabase =
            Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .addCallback(RoomCallback)
                .build()
    }

    companion object {
        private const val DATABASE_NAME = "BanterboxSeller.db"

        /* Tables Names */
        const val TABLE_UPLOAD_SESSION = "upload_session"
        const val TABLE_UPLOAD_FILES = "upload_files"
        const val TABLE_NOTIFICATION = "notification"

        const val TABLE_CACHE_KEYS = "cache_keys"

        private val RoomCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate the database if required.
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                // Handle account based reset on destructive migration.
            }
        }
    }
}