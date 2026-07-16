package com.prohub.assistant.di

import android.content.Context
import androidx.room.Room
import com.prohub.assistant.data.db.AppDatabase
import com.prohub.assistant.data.db.FitnessDao
import com.prohub.assistant.data.db.FitnessGoalDao
import com.prohub.assistant.data.db.NotificationDao
import com.prohub.assistant.data.db.SummaryDao
import com.prohub.assistant.data.db.TodoDao
import com.prohub.assistant.data.repository.FitnessRepository
import com.prohub.assistant.data.repository.NotificationRepository
import com.prohub.assistant.data.repository.TodoRepository
import com.prohub.assistant.data.prefs.SecurePrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        securePrefs: SecurePrefs
    ): AppDatabase {
        System.loadLibrary("sqlcipher")
        val factory = SupportOpenHelperFactory(securePrefs.getDbPassphrase())
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "prohub_database"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideSummaryDao(db: AppDatabase): SummaryDao = db.summaryDao()

    @Provides
    fun provideTodoDao(db: AppDatabase): TodoDao = db.todoDao()

    @Provides
    fun provideFitnessDao(db: AppDatabase): FitnessDao = db.fitnessDao()

    @Provides
    fun provideFitnessGoalDao(db: AppDatabase): FitnessGoalDao = db.fitnessGoalDao()

    @Provides
    @Singleton
    fun provideSecurePrefs(@ApplicationContext context: Context): SecurePrefs {
        return SecurePrefs(context)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(dao: NotificationDao, summaryDao: SummaryDao): NotificationRepository {
        return NotificationRepository(dao, summaryDao)
    }

    @Provides
    @Singleton
    fun provideTodoRepository(dao: TodoDao): TodoRepository {
        return TodoRepository(dao)
    }

    @Provides
    @Singleton
    fun provideFitnessRepository(
        fitnessDao: FitnessDao,
        goalDao: FitnessGoalDao
    ): FitnessRepository {
        return FitnessRepository(fitnessDao, goalDao)
    }
}