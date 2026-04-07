package com.mm.astraisandroid.data.repository

import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.TareaResponse
import com.mm.astraisandroid.data.local.dao.TareaDao
import com.mm.astraisandroid.data.local.entities.TareaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Gestiona la lógica de datos de tareas
 */
class TareaRepository(
    private val api: BackendRepository, // Acceso a Ktor
    private val tareaDao: TareaDao      // Acceso a Room
) {
    val allTareas: Flow<List<TareaEntity>> = tareaDao.getAllTareas()

    /**
     * Intenta actualizar los datos desde el servidor
     * Si hay éxito, actualiza Room. Si falla significa que está offline y no hacemos nada
     */
    suspend fun refreshTareas(gid: Int): Result<Unit> {
        return try {
            val result = api.getTareas(gid)
            val entities = result.map { it.toEntity() }
            tareaDao.clearSyncedTareas()
            tareaDao.insertTareas(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Mapea el DTO a entidad
 */
fun TareaResponse.toEntity() = TareaEntity(
    id = this.id,
    titulo = this.titulo,
    descripcion = this.descripcion,
    tipo = this.tipo,
    estado = this.estado,
    prioridad = this.prioridad,
    recompensaXp = this.recompensaXp,
    recompensaLudion = this.recompensaLudion
)