package com.example.proyekakhir.notifikasi


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NOTIF_DEBUG", "BootReceiver dipanggil dengan action: ${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("NOTIF_DEBUG", "Device reboot → jalankan worker")

            val workRequest = OneTimeWorkRequestBuilder<KegiatanWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}