package com.mm.astraisandroid.util.logging

/**
 * Cada valor define un tag identificativo usado para filtrar y categorizar los logs en el sistema.
 *
 * @property tag Etiqueta de texto asociada al módulo para los logs.
 */
enum class LogFeature(val tag: String) {
    /** Módulo de autenticación de usuarios. */
    AUTH("AstraisAuth"),
    /** Módulo de gestión de tareas. */
    TASKS("AstraisTasks"),
    /** Módulo de gestión de grupos. */
    GROUPS("AstraisGroups"),
    /** Módulo de tienda. */
    STORE("AstraisStore"),
    /** Módulo de perfil y datos de usuario. */
    USER("AstraisUser"),
    /** Módulo de operaciones de red. */
    NETWORK("AstraisNetwork"),
    /** Módulo de sincronización de datos. */
    SYNC("AstraisSync"),
    /** Módulo de interfaz de usuario. */
    UI("AstraisUI")
}
