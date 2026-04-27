package com.mm.astraisandroid.data.repository

import com.mm.astraisandroid.data.api.CreateTareaHabitData
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.api.CreateTareaUniqueData
import com.mm.astraisandroid.data.api.EditTareaRequest
import com.mm.astraisandroid.data.api.HabitFrequency
import com.mm.astraisandroid.data.api.services.TaskApi
import com.mm.astraisandroid.data.api.toEntity
import com.mm.astraisandroid.data.local.dao.TareaDao
import com.mm.astraisandroid.data.local.entities.toDomain
import com.mm.astraisandroid.data.models.TaskPriority
import com.mm.astraisandroid.data.models.Task
import com.mm.astraisandroid.data.models.TaskType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * Gestiona la lógica de datos de tareas
 */
class TaskRepository @Inject constructor(
    private val api: TaskApi,
    private val tareaDao: TareaDao
) {
    val allTareas: Flow<List<Task>> = tareaDao.getAllTareas().map { entidades ->
        entidades.map { it.toDomain() }
    }

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

    suspend fun completarTarea(tid: Int) {
        api.completarTarea(tid)
    }

    suspend fun createNewTask(
        gid: Int,
        title: String,
        description: String,
        type: TaskType,
        priority: TaskPriority,
        dueDate: String? = null,
        frecuencia: HabitFrequency? = null,
        parentId: Int? = null
    ) {
        val networkPriority = priority.ordinal

        val extraUnico = if (type == TaskType.UNICO) {
            val safeDate = if (!dueDate.isNullOrBlank()) "${dueDate}T23:59:59Z" else "2026-12-31T23:59:59Z"
            CreateTareaUniqueData(fechaLimite = safeDate)
        } else null

        val extraHabito = if (type == TaskType.HABITO && frecuencia != null) {
            CreateTareaHabitData(
                frequency = frecuencia,
                numeroFrecuencia = 1
            )
        } else null

        val request = CreateTareaRequest(
            gid = gid,
            titulo = title,
            descripcion = description,
            tipo = type.name,
            prioridad = networkPriority,
            extraUnico = extraUnico,
            extraHabito = extraHabito,
            idObjetivo = parentId
        )

        api.createTarea(request)
    }

    suspend fun createTareaDirect(request: CreateTareaRequest): Int {
        return api.createTarea(request)
    }

    suspend fun eliminarTarea(tid: Int) {
        api.deleteTarea(tid)
    }

    suspend fun editarTarea(
        tid: Int,
        titulo: String?,
        descripcion: String?,
        prioridad: TaskPriority?,
        dueDate: String? = null,
        frecuencia: HabitFrequency? = null
    ) {
        val extraUnico = if (dueDate != null) CreateTareaUniqueData(dueDate) else null
        val extraHabito = if (frecuencia != null) CreateTareaHabitData(1, frecuencia) else null

        val req = EditTareaRequest(
            titulo = titulo,
            descripcion = descripcion,
            prioridad = prioridad?.ordinal,
            extraUnico = extraUnico,
            extraHabito = extraHabito
        )
        api.editarTarea(tid, req)
    }

    suspend fun uncompleteTarea(tid: Int) {
        api.uncompleteTarea(tid)
    }
}
