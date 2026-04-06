package com.astrais

import kotlinx.serialization.Serializable

enum class ErrorCodes {
    /** El token pasado no tiene validez */
    ERR_INVALIDTOKEN,
    /** El mensaje enviado al servidor esta malformado */
    ERR_MALFORMEDMESSAGE,
    /** La operacion no fue implementada */
    ERR_UNIMPLEMENTED,
    /** El recurso no fue encontrado por el servidor */
    ERR_RESOURCEMISSING,
    /** El recurso ya existe, y no se puede crear de nuevo */
    ERR_RESOURCEALREADYEXISTS,
    /** Alguno de los valores en el JSON esta vacio, y eso no se permite */
    ERR_BLANKVALUE,
    /** El valor no esta vacio, pero no fue aceptado por el backend */
    ERR_BADVALUE,
    /** El recurso no se puede crear */
    ERR_RESOURCENOTCREATED,
    /** No se permite el acceso a la ruta */
    EER_FORBIDDEN,
    /** Error interno importante */
    ERR_INTERNALERROR
}

@Serializable
data class Errors(
    val errorCode : Int,
    val errorText : String
)