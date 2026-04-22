package com.example.proyekakhir.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.proyekakhir.ui.fragment.Kalender
import com.example.proyekakhir.ui.fragment.Profil
import com.example.proyekakhir.R
import com.example.proyekakhir.auth.LoginActivity
import com.example.proyekakhir.databinding.ActivityMainBinding
import com.example.proyekakhir.notifikasi.KegiatanWorker
import com.example.proyekakhir.notifikasi.NotificationScheduler
import com.example.proyekakhir.ui.fragment.Home
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cek token
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        val token = shared.getString("TOKEN", null)
        if (token.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Set warna icon bottom nav via kode
        val colorStateList = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                getColor(R.color.blue),  // warna aktif
                0xFF9E9E9E.toInt()       // warna tidak aktif (abu)
            )
        )
        binding.bottomNavigation.itemIconTintList = colorStateList
        binding.bottomNavigation.itemTextColor = colorStateList

        // Tampilkan HomeFragment saat pertama buka
        if (savedInstanceState == null) {
            loadFragment(Home())
        }

        // Handle bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { loadFragment(Home()); true }
                R.id.nav_kalender -> { loadFragment(Kalender()); true }
                R.id.nav_profile -> { loadFragment(Profil()); true }
                else -> false
            }
        }

        // 🔔 Permission notifikasi Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        startWorker() // ← jalankan worker setelah permission

//        NotificationScheduler.scheduleNotification(
//            this,
//            "TEST NOTIF",
//            "Notif muncul 5 detik",
//            System.currentTimeMillis() + 5000,
//            999
//        )
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun startWorker() {
        val work = PeriodicWorkRequestBuilder<KegiatanWorker>(15, TimeUnit.MINUTES).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "kegiatan_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            work
        )
    }
}