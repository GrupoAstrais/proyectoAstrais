package com.mm.astraisandroid

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

/**
 * Proveedor del widget de pantalla de inicio para la aplicación Astrais.
 *
 * Actualiza las instancias del widget con el conteo de tareas pendientes cuando
 * el sistema lo solicita.
 */
class AstraisWidget : AppWidgetProvider() {
    /**
     * Llamado cuando el AppWidget manager solicita una actualización de las instancias del widget.
     *
     * @param context Contexto en el que se ejecuta el widget.
     * @param appWidgetManager AppWidgetManager responsable de actualizar las vistas del widget.
     * @param appWidgetIds Array de IDs de widgets que necesitan ser actualizados.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }
}

/**
 * Actualiza una instancia del widget con el conteo actual de tareas pendientes.
 *
 * @param context Contexto usado para acceder a recursos y nombre del paquete.
 * @param appWidgetManager AppWidgetManager usado para enviar actualizaciones de vista.
 * @param widgetId ID de la instancia específica del widget a actualizar.
 */
fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.widget_layout)

    views.setTextViewText(R.id.widget_subtitle, context.getString(R.string.widget_pending_tasks, 3))

    appWidgetManager.updateAppWidget(widgetId, views)
}