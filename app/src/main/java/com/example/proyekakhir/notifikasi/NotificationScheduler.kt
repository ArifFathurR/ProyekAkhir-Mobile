package com.example.proyekakhir.notifikasi

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.proyekakhir.R
import com.example.proyekakhir.ui.MainActivity

object NotificationScheduler {

    private const val CHANNEL_ID = "kegiatan_channel"

    fun showNotification(context: Context, title: String, message: String) {
        Log.d("NOTIF_DEBUG", "Menampilkan notifikasi: $title | $message")

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kegiatan",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notifId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        manager.notify(notifId, notif)
    }

    fun scheduleNotification(
        context: Context,
        title: String,
        message: String,
        time: Long,
        id: Int
    ) {
        Log.d("NOTIF_DEBUG", "Scheduling notif ID=$id at $time ($title)")

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            flags = Intent.FLAG_RECEIVER_FOREGROUND
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ✅ FIX: gunakan ini (tidak perlu permission exact alarm)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }
}