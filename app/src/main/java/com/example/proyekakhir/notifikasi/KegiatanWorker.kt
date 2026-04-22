package com.example.proyekakhir.notifikasi

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.model.Kegiatan
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class KegiatanWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {
        Log.d("NOTIF_DEBUG", "Worker dijalankan")

        val prefs = applicationContext.getSharedPreferences("APP", Context.MODE_PRIVATE)
        val token = prefs.getString("TOKEN", "") ?: ""

        if (token.isEmpty()) {
            Log.d("NOTIF_DEBUG", "Token kosong → retry")
            return Result.retry()
        }

        try {
            Log.d("NOTIF_DEBUG", "Ambil data kegiatan dari API")

            val response = ApiClient.instance.getKegiatan("Bearer $token").execute()

            if (response.isSuccessful && response.body() != null) {
                val kegiatanList = response.body()!!.kegiatan

                Log.d("NOTIF_DEBUG", "Jumlah kegiatan: ${kegiatanList.size}")

                scheduleNotifications(kegiatanList)
            } else {
                Log.d("NOTIF_DEBUG", "Response gagal")
                return Result.retry()
            }

        } catch (e: Exception) {
            Log.e("NOTIF_DEBUG", "Error: ${e.message}")
            return Result.retry()
        }

        return Result.success()
    }

    private fun scheduleNotifications(list: List<Kegiatan>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = System.currentTimeMillis()

        list.forEach { k ->
            try {
                val dateTime = "${k.tanggal} ${k.waktu}"
                val eventTime = sdf.parse(dateTime)?.time ?: return@forEach

                Log.d("NOTIF_DEBUG", "Kegiatan: ${k.nama_kegiatan} waktu=$eventTime")

                scheduleReminder(k, eventTime, -3, "H-3", now)
                scheduleReminder(k, eventTime, -2, "H-2", now)
                scheduleReminder(k, eventTime, -1, "H-1", now)

            } catch (e: Exception) {
                Log.e("NOTIF_DEBUG", "Parse error: ${e.message}")
            }
        }
    }
    private fun scheduleReminder(
        k: Kegiatan,
        eventTime: Long,
        offsetDay: Int,
        label: String,
        now: Long
    ) {
        val cal = Calendar.getInstance().apply { timeInMillis = eventTime }
        cal.add(Calendar.DAY_OF_YEAR, offsetDay)

        val triggerTime = cal.timeInMillis

        val title = "Pengingat Kegiatan ($label)"
        val message = "${k.nama_kegiatan} - ${k.sub_kegiatan}"

        Log.d("NOTIF_DEBUG", "Cek trigger $label → $triggerTime")

        if (triggerTime > now) {
            Log.d("NOTIF_DEBUG", "Jadwalkan notif $label untuk ${k.nama_kegiatan}")

            NotificationScheduler.scheduleNotification(
                applicationContext,
                title,
                message,
                triggerTime,
                k.id * 10 + offsetDay
            )
        } else {
            Log.d("NOTIF_DEBUG", "Lewat waktu → skip $label")
        }
    }


}