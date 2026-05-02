package com.mm.astraisandroid.di

import android.content.Context
import com.mm.astraisandroid.data.api.createHttpClient
import com.mm.astraisandroid.data.api.services.AuthApi
import com.mm.astraisandroid.data.api.services.StoreApi
import com.mm.astraisandroid.data.api.services.TaskApi
import com.mm.astraisandroid.data.api.services.UserApi
import com.mm.astraisandroid.data.local.AstraisDb
import com.mm.astraisandroid.data.local.dao.ActionDao
import com.mm.astraisandroid.data.local.dao.TareaDao
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.repository.AuthRepository
import com.mm.astraisandroid.data.repository.SessionOrchestrator
import com.mm.astraisandroid.data.repository.StoreRepository
import com.mm.astraisandroid.data.repository.TaskRepository
import com.mm.astraisandroid.data.repository.UserRepository
import com.mm.astraisandroid.data.api.services.GroupApi
import com.mm.astraisandroid.data.local.dao.GrupoDao
import com.mm.astraisandroid.data.preferences.SessionManagerImpl
import com.mm.astraisandroid.data.repository.GroupRepository
import com.mm.astraisandroid.ui.components.SnackbarManager
import com.mm.astraisandroid.ui.components.SnackbarManagerImpl
import com.mm.astraisandroid.util.logging.AppLogger
import com.mm.astraisandroid.util.logging.TimberAppLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(sessionManager: SessionManager, json: Json, appLogger: AppLogger): HttpClient {
        return createHttpClient(sessionManager, json, appLogger)
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
    fun provideGroupApi(httpClient: HttpClient): GroupApi {
        return GroupApi(httpClient)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(api: TaskApi, dao: TareaDao, actionDao: ActionDao): TaskRepository {
        return TaskRepository(api, dao, actionDao)
    }

    @Provides
    @Singleton
    fun provideUserRepository(api: UserApi): UserRepository {
        return UserRepository(api)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApi,
        userApi: UserApi,
        sessionManager: SessionManager,
        sessionOrchestrator: SessionOrchestrator,
        appLogger: AppLogger
    ): AuthRepository {
        return AuthRepository(api, userApi, sessionManager, sessionOrchestrator, appLogger)
    }

    @Provides
    @Singleton
    fun provideStoreRepository(api: StoreApi): StoreRepository {
        return StoreRepository(api)
    }

    @Provides
    @Singleton
    fun provideGroupRepository(api: GroupApi, dao: GrupoDao): GroupRepository {
        return GroupRepository(api, dao)
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

    @Provides
    fun provideGrupoDao(db: AstraisDb): GrupoDao {
        return db.grupoDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceBindingModule {

    @Binds
    @Singleton
    abstract fun bindSnackbarManager(
        snackbarManagerImpl: SnackbarManagerImpl
    ): SnackbarManager

    @Binds
    @Singleton
    abstract fun bindSessionManager(
        sessionManagerImpl: SessionManagerImpl
    ): SessionManager

    @Binds
    @Singleton
    abstract fun bindAppLogger(
        timberAppLogger: TimberAppLogger
    ): AppLogger
}
