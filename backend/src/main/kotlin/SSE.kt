import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.time.Duration.Companion.seconds

const val SSEEVENT_RELOADSTORE = "RELOAD.STORE"

data class SSEMessage(
    val data: String,
    val event: String
)

object GlobalBusSSE {
    private val _events = MutableSharedFlow<SSEMessage>()
    public val events : SharedFlow<SSEMessage> = _events

    suspend fun publish(message : SSEMessage) {
        _events.emit(message)
    }
}

fun Application.installSSE(){
    install(SSE)
}

fun Route.sseRoutes() {
    sse("/events/global") {
        GlobalBusSSE.events.collect {msg->
            send(
                ServerSentEvent(
                    data = msg.data,
                    event = msg.event
                )
            )
        }
    }
}