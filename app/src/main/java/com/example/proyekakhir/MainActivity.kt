package com.example.proyekakhir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyekakhir.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil token dari SharedPreferences
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        token = shared.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

//        // Ambil profil user dari API
//        ApiClient.instance.getProfile("Bearer $token").enqueue(object : Callback<User> {
//            override fun onResponse(call: Call<User>, response: Response<User>) {
//                if (response.isSuccessful) {
//                    val user = response.body()
//                    binding.tvWelcome.text = "Halo, ${user?.name}"
//                } else {
//                    Toast.makeText(this@MainActivity, "Gagal mengambil data profil", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<User>, t: Throwable) {
//                Toast.makeText(this@MainActivity, "Kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            ApiClient.instance.logout("Bearer $token").enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    shared.edit().remove("TOKEN").apply()
                    Toast.makeText(this@MainActivity, "Logout berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Gagal logout: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
