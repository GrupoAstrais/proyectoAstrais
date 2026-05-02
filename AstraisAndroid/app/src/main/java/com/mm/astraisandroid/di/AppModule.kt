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

/**
 * Módulo de proveedores de Hilt para la capa de datos y servicios de Astrais.
 *
 * Define las instancias singleton de serialización JSON, cliente HTTP,
 * APIs de red, repositorios y base de datos local.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Proporciona una instancia configurada de [Json] para serialización/deserialización.
     *
     * @return Instancia de [Json] tolerante a claves desconocidas.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Proporciona el cliente HTTP de Ktor configurado con interceptores de autenticación
     * y logging.
     *
     * @param sessionManager Gestor de sesión para inyectar tokens en las peticiones.
     * @param json Instancia de [Json] para serialización de cuerpos.
     * @param appLogger Logger estructurado para trazabilidad de peticiones.
     * @return Cliente HTTP listo para uso por las APIs de la aplicación.
     */
    @Provides
    @Singleton
    fun provideHttpClient(sessionManager: SessionManager, json: Json, appLogger: AppLogger): HttpClient {
        return createHttpClient(sessionManager, json, appLogger)
    }

    /**
     * Proporciona la API de tareas.
     *
     * @param httpClient Cliente HTTP inyectado.
     * @return Instancia de [TaskApi].
     */
    @Provides
    @Singleton
    fun provideTaskApi(httpClient: HttpClient): TaskApi {
        return TaskApi(httpClient)
    }

    /**
     * Proporciona la API de autenticación.
     *
     * @param httpClient Cliente HTTP inyectado.
     * @return Instancia de [AuthApi].
     */
    @Provides
    @Singleton
    fun provideAuthApi(httpClient: HttpClient): AuthApi {
        return AuthApi(httpClient)
    }

    /**
     * Proporciona la API de usuario/perfil.
     *
     * @param httpClient Cliente HTTP inyectado.
     * @return Instancia de [UserApi].
     */
    @Provides
    @Singleton
    fun provideUserApi(httpClient: HttpClient): UserApi {
        return UserApi(httpClient)
    }

    /**
     * Proporciona la API de tienda/cosméticos.
     *
     * @param httpClient Cliente HTTP inyectado.
     * @return Instancia de [StoreApi].
     */
    @Provides
    @Singleton
    fun provideStoreApi(httpClient: HttpClient): StoreApi {
        return StoreApi(httpClient)
    }

    /**
     * Proporciona la API de grupos.
     *
     * @param httpClient Cliente HTTP inyectado.
     * @return Instancia de [GroupApi].
     */
    @Provides
    @Singleton
    fun provideGroupApi(httpClient: HttpClient): GroupApi {
        return GroupApi(httpClient)
    }

    /**
     * Proporciona el repositorio de tareas.
     *
     * @param api API de tareas.
     * @param dao DAO local de tareas.
     * @param actionDao DAO de acciones pendientes.
     * @return Instancia de [TaskRepository].
     */
    @Provides
    @Singleton
    fun provideTaskRepository(api: TaskApi, dao: TareaDao, actionDao: ActionDao): TaskRepository {
        return TaskRepository(api, dao, actionDao)
    }

    /**
     * Proporciona el repositorio de usuario.
     *
     * @param api API de usuario.
     * @return Instancia de [UserRepository].
     */
    @Provides
    @Singleton
    fun provideUserRepository(api: UserApi): UserRepository {
        return UserRepository(api)
    }

    /**
     * Proporciona el repositorio de autenticación.
     *
     * @param api API de autenticación.
     * @param userApi API de usuario (para obtener datos tras login).
     * @param sessionManager Gestor de sesión.
     * @param sessionOrchestrator Orquestador de sesiones.
     * @param appLogger Logger estructurado.
     * @return Instancia de [AuthRepository].
     */
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

    /**
     * Proporciona el repositorio de tienda.
     *
     * @param api API de tienda.
     * @return Instancia de [StoreRepository].
     */
    @Provides
    @Singleton
    fun provideStoreRepository(api: StoreApi): StoreRepository {
        return StoreRepository(api)
    }

    /**
     * Proporciona el repositorio de grupos.
     *
     * @param api API de grupos.
     * @param dao DAO local de grupos.
     * @return Instancia de [GroupRepository].
     */
    @Provides
    @Singleton
    fun provideGroupRepository(api: GroupApi, dao: GrupoDao): GroupRepository {
        return GroupRepository(api, dao)
    }

    /**
     * Proporciona la instancia singleton de la base de datos Room.
     *
     * @param context Contexto de la aplicación.
     * @return Instancia de [AstraisDb].
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AstraisDb {
        return AstraisDb.getInstance(context)
    }

    /**
     * Proporciona el DAO de tareas desde la base de datos.
     *
     * @param db Instancia de la base de datos.
     * @return DAO de tareas.
     */
    @Provides
    fun provideTareaDao(db: AstraisDb): TareaDao {
        return db.tareaDao()
    }

    /**
     * Proporciona el DAO de acciones pendientes desde la base de datos.
     *
     * @param db Instancia de la base de datos.
     * @return DAO de acciones.
     */
    @Provides
    fun provideActionDao(db: AstraisDb): ActionDao {
        return db.actionDao()
    }

    /**
     * Proporciona el DAO de grupos desde la base de datos.
     *
     * @param db Instancia de la base de datos.
     * @return DAO de grupos.
     */
    @Provides
    fun provideGrupoDao(db: AstraisDb): GrupoDao {
        return db.grupoDao()
    }
}

/**
 * Módulo de bindings abstractos de Hilt para interfaces que requieren una única
 * implementación en tiempo de compilación.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceBindingModule {

    /**
     * Vincula la implementación concreta de [SnackbarManager].
     */
    @Binds
    @Singleton
    abstract fun bindSnackbarManager(
        snackbarManagerImpl: SnackbarManagerImpl
    ): SnackbarManager

    /**
     * Vincula la implementación concreta de [SessionManager].
     */
    @Binds
    @Singleton
    abstract fun bindSessionManager(
        sessionManagerImpl: SessionManagerImpl
    ): SessionManager

    /**
     * Vincula la implementación concreta de [AppLogger].
     */
    @Binds
    @Singleton
    abstract fun bindAppLogger(
        timberAppLogger: TimberAppLogger
    ): AppLogger
}
