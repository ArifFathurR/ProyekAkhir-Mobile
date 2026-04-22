package com.example.proyekakhir.notifikasi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Pengingat"
        val message = intent.getStringExtra("message") ?: "Ada kegiatan"

        Log.d("NOTIF_DEBUG", "Receiver terpanggil: $title | $message")

        NotificationScheduler.showNotification(context, title, message)
    }
}