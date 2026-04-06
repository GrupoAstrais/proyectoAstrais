package com.mm.astraisandroid.di

import android.content.Context
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.client
import com.mm.astraisandroid.data.local.AstraisDb
import com.mm.astraisandroid.data.local.dao.ActionDao
import com.mm.astraisandroid.data.local.dao.TareaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return client
    }

    @Provides
    @Singleton
    fun provideBackendRepository(httpClient: HttpClient): BackendRepository {
        return BackendRepository(httpClient)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AstraisDb {
        return AstraisDb.getInstance(context)
    }

    @Provides
    fun provideTareaDao(db: AstraisDb): TareaDao {
        return db.tareaDao()
    }

    @Provides
    fun provideActionDao(db: AstraisDb): ActionDao {
        return db.actionDao()
    }
}