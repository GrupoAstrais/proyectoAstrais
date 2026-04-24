package com.mm.astraisandroid.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mm.astraisandroid.data.local.dao.ActionDao
import com.mm.astraisandroid.data.local.dao.TareaDao
import com.mm.astraisandroid.data.local.entities.PendingAction
import com.mm.astraisandroid.data.local.entities.TareaEntity
import com.mm.astraisandroid.data.local.entities.GrupoEntity
import com.mm.astraisandroid.data.local.dao.GrupoDao

/**
 * Punto de acceso principal a la base de datos local utilizando Room.
 *
 * Esta base de datos gestiona dos tablas fundamentales:
 * 1. [TareaEntity]: Almacena las tareas del usuario para acceso rápido y modo offline.
 * 2. [PendingAction]: Registra las acciones realizadas sin conexión que deben sincronizarse con el servidor.
 *
 * @see TareaDao
 * @see ActionDao
 */
@Database(entities = [TareaEntity::class, PendingAction::class, GrupoEntity::class], version = 2, exportSchema = false)
abstract class AstraisDb : RoomDatabase() {

    /**
     * Acceso a las operaciones de datos para la entidad de Tareas.
     * @return El DAO encargado de la gestión de tareas.
     */
    abstract fun tareaDao(): TareaDao

    abstract fun grupoDao(): GrupoDao

    /**
     * Acceso a las operaciones de datos para la cola de acciones pendientes.
     * @return El DAO encargado de la gestión de sincronización offline.
     */
    abstract fun actionDao(): ActionDao

    companion object {
        /**
         * Instancia volátil para asegurar que los cambios en [INSTANCE] sean visibles
         * de inmediato para todos los hilos de ejecución.
         */
        @Volatile
        private var INSTANCE: AstraisDb? = null

        /**
         * Recupera la instancia única de la base de datos.
         *
         * Se utiliza una construcción sincronizada para evitar condiciones de carrera
         * si múltiples hilos intentan crear la base de datos al mismo tiempo.
         *
         * @param context Contexto de la aplicación necesario para inicializar Room.
         * @return La instancia activa de [AstraisDb].
         */
        fun getInstance(context: Context): AstraisDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstraisDb::class.java,
                    "astrais.db"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}