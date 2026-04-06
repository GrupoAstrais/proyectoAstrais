import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.time.Duration.Companion.seconds

const val SSEEVENT_RELOADSTORE = "RELOAD.STORE"

/**
 * Mensaje SSE que se puede enviar
 */
data class SSEMessage(
    /** El identificador del evento SSE */
    val event: String,
    /** Los datos del evento SSE */
    val data: String
)

object GlobalBusSSE {
    private val _events = MutableSharedFlow<SSEMessage>()
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
    suspend fun publishStoreReload() = publish(SSEMessage(event = SSEEVENT_RELOADSTORE, data = ""))
}

fun Application.installSSE(){
    install(SSE)
}

fun Route.sseRoutes() {
    sse("/events/global") {
        GlobalBusSSE.events.collect { msg->
            send(
                ServerSentEvent(
                    data = msg.data,
                    event = msg.event
                )
            )
        }
    }
}