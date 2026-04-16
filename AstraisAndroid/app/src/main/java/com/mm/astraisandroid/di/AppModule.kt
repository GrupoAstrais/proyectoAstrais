package com.mm.astraisandroid.di

import android.content.Context
import com.mm.astraisandroid.data.api.client
import com.mm.astraisandroid.data.api.services.AuthApi
import com.mm.astraisandroid.data.api.services.StoreApi
import com.mm.astraisandroid.data.api.services.TaskApi
import com.mm.astraisandroid.data.api.services.UserApi
import com.mm.astraisandroid.data.local.AstraisDb
import com.mm.astraisandroid.data.local.dao.ActionDao
import com.mm.astraisandroid.data.local.dao.TareaDao
import com.mm.astraisandroid.data.repository.AuthRepository
import com.mm.astraisandroid.data.repository.StoreRepository
import com.mm.astraisandroid.data.repository.TaskRepository
import com.mm.astraisandroid.data.repository.UserRepository
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
    fun provideTaskApi(httpClient: HttpClient): TaskApi {
        return TaskApi(httpClient)
    }

    @Provides
    @Singleton
    fun provideAuthApi(httpClient: HttpClient): AuthApi {
        return AuthApi(httpClient)
    }

    @Provides
    @Singleton
    fun provideUserApi(httpClient: HttpClient): UserApi {
        return UserApi(httpClient)
    }

    @Provides
    @Singleton
    fun provideStoreApi(httpClient: HttpClient): StoreApi {
        return StoreApi(httpClient)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(api: TaskApi, dao: TareaDao): TaskRepository {
        return TaskRepository(api, dao)
    }

    @Provides
    @Singleton
    fun provideUserRepository(api: UserApi): UserRepository {
        return UserRepository(api)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi, userApi: UserApi): AuthRepository {
        return AuthRepository(api, userApi)
    }

    @Provides
    @Singleton
    fun provideStoreRepository(api: StoreApi): StoreRepository {
        return StoreRepository(api)
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