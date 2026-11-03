package com.example.gestarbien.notificaciones

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gestarbien.MainActivity
import com.example.gestarbien.R

/**
 * Programa y cancela las alarmas de los recordatorios de citas y controles.
 *
 * AndroidManifest.xml necesita:
 *   <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
 *   <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
 *   <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
 *   <receiver android:name=".notificaciones.RecordatorioReceiver" android:exported="false" />
 */
object RecordatoriosScheduler {

    private const val TAG = "Recordatorios"

    const val CANAL_ID     = "gestarbien_recordatorios_v3"
    private const val CANAL_NOMBRE = "Recordatorios de citas"
    const val EXTRA_TITULO = "extra_titulo"
    const val EXTRA_TEXTO  = "extra_texto"
    const val EXTRA_ID     = "extra_id"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val soundUri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
        val audioAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val canal = NotificationChannel(
            CANAL_ID,
            CANAL_NOMBRE,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos de tus controles prenatales y citas"
            enableVibration(true)
            setSound(soundUri, audioAttributes)
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(canal)
    }

    /** true si el sistema permite alarmas exactas (Android 12+ lo puede negar). */
    fun puedeAlarmasExactas(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun solicitarPermisoAlarmaExacta(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !puedeAlarmasExactas(context)) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }

    fun solicitarExcepcionBateria(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }

    /**
     * @return false si el momento del aviso ya pasó (no se programa nada).
     */
    fun programar(
        context: Context,
        id: Int,
        titulo: String,
        texto: String,
        cuandoMs: Long
    ): Boolean {

        if (cuandoMs <= System.currentTimeMillis()) {
            Log.w(TAG, "Aviso descartado: el momento ya pasó ($cuandoMs)")
            return false
        }

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, RecordatorioReceiver::class.java).apply {
            putExtra(EXTRA_TITULO, titulo)
            putExtra(EXTRA_TEXTO, texto)
            putExtra(EXTRA_ID, id)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (puedeAlarmasExactas(context)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cuandoMs, pending)
                Log.d(TAG, "Alarma exacta programada para $cuandoMs (id=$id)")
            } else {
                am.setWindow(AlarmManager.RTC_WAKEUP, cuandoMs, 15 * 60 * 1000L, pending)
                Log.d(TAG, "Alarma aproximada programada para $cuandoMs (id=$id)")
            }
        } catch (e: SecurityException) {
            am.setWindow(AlarmManager.RTC_WAKEUP, cuandoMs, 15 * 60 * 1000L, pending)
            Log.w(TAG, "Sin permiso de alarma exacta, se usó ventana aproximada", e)
        }

        return true
    }

    fun cancelar(context: Context, id: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = PendingIntent.getBroadcast(
            context,
            id,
            Intent(context, RecordatorioReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pending)
        pending.cancel()
        Log.d(TAG, "Alarma cancelada (id=$id)")
    }

    /** Notificación inmediata. Sirve para comprobar que todo el circuito funciona. */
    fun notificarAhora(context: Context, titulo: String, texto: String) {
        crearCanal(context)
        lanzarNotificacion(context, 9999, titulo, texto)
    }

    internal fun lanzarNotificacion(context: Context, id: Int, titulo: String, texto: String) {
        val abrirApp = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion: Notification = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setColor(android.graphics.Color.parseColor("#B1B4E1"))
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notificacion)
            Log.d(TAG, "Notificación lanzada (id=$id)")
        } catch (e: SecurityException) {
            Log.e(TAG, "Falta el permiso POST_NOTIFICATIONS", e)
        }
    }
}

/** Recibe la alarma y lanza la notificación. */
class RecordatorioReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra(RecordatoriosScheduler.EXTRA_TITULO) ?: "Recordatorio"
        val texto  = intent.getStringExtra(RecordatoriosScheduler.EXTRA_TEXTO) ?: ""
        val id     = intent.getIntExtra(RecordatoriosScheduler.EXTRA_ID, 0)

        Log.d("Recordatorios", "Receiver disparado (id=$id)")

        RecordatoriosScheduler.crearCanal(context)
        RecordatoriosScheduler.lanzarNotificacion(context, id, titulo, texto)
    }
}