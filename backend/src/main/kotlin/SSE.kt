import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


enum class SSEEventType(val value : String) {
    /** Evento global de recarga de tienda */
    RELOAD_STORE("RELOAD.STORE"),
    /** Evento por persona de agregar tarea */
    ADD_TASK("ADDED.TASK"),
    /** Evento por persona para hacerle cerrar sesion */
    SIGN_OFF("SIGN.OFF")
    ;

    companion object {
        fun fromValue(v: String) : SSEEventType? = entries.find { it.value.equals(v, ignoreCase = true) }
    }
}

/**
 * Evento SSE de añadir tarea
 */
@Serializable
data class SSEEventAddTaskData(
    /** GID del grupo afectado */
    val gid : Int,
    /** ID de la tarea afectada */
    val tid : Int
)

/**
 * Mensaje SSE que se puede enviar
 */
data class SSEMessage(
    /** El identificador del evento SSE */
    val event: SSEEventType,
    /** Los datos del evento SSE */
    val data: String
)

const val GLOBAL_BUFFER_CAPACITY = 10
const val USER_BUFFER_CAPACITY = 10

object GlobalBusSSE {
    private val _events = MutableSharedFlow<SSEMessage>(extraBufferCapacity = GLOBAL_BUFFER_CAPACITY)
    /** Sharedflow para los mensajes SSE que se envian */
    public val events : SharedFlow<SSEMessage> = _events

    /**
     * Se publica un evento SSE global en la queue
     * @param message El mensaje SSE a publicar
     */
    suspend fun publish(message : SSEMessage) {
        _events.emit(message)
    }

    /**
     * Shortcut para enviar eventos de recargado de tienda
     */
    suspend fun publishStoreReload() = publish(SSEMessage(event = SSEEventType.RELOAD_STORE, data = ""))
}

object UserBusSSE {
    private val _events = MutableSharedFlow<Pair<Int, SSEMessage>>(extraBufferCapacity = USER_BUFFER_CAPACITY * 10)
    public val events: SharedFlow<Pair<Int, SSEMessage>> = _events

    /**
     * Se emite un mensaje para un usuario en especifico
     */
    public suspend fun publish(uid: Int, message: SSEMessage) {
        _events.emit(uid to message)
    }

    /**
     * Shortcut para el evento de agregar tareas
     */
    public suspend fun publishAddTask(uid : Int, data : SSEEventAddTaskData) = publish(
        uid = uid,
        message = SSEMessage(
            event = SSEEventType.ADD_TASK,
            data = Json.encodeToString(data)
        )
    )

    /**
     * Shortcut para el evento de agregar tareas
     */
    public suspend fun publishSignOff(uid : Int) = publish(
        uid = uid,
        message = SSEMessage(
            event = SSEEventType.SIGN_OFF,
            data = ""
        )
    )
}


fun Application.installSSE(){
    install(SSE)
}

fun Route.sseRoutes() {
    sse("/events/global") {
        try {
            GlobalBusSSE.events.collect { msg ->
                if (!currentCoroutineContext().isActive) return@collect
                send(
                    ServerSentEvent(
                        data = msg.data,
                        event = msg.event.value
                    )
                )
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Desconectar cliente
        }
    }
    authenticate("access-jwt") {
        sse("/events/user") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1){
                return@sse
            }

            try {
                UserBusSSE.events.collect { (targetUid, msg) ->
                    if (!currentCoroutineContext().isActive) return@collect
                    if (targetUid == uid) {
                        send(
                            ServerSentEvent(
                                data = msg.data,
                                event = msg.event.value
                            )
                        )
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Desconectar cliente
            } finally {
                UserBusSSE.publishSignOff(uid)
            }
        }
    }
}