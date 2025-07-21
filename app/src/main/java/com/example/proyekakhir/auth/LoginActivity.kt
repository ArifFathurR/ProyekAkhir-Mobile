package com.example.proyekakhir.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyekakhir.ApiClient
import com.example.proyekakhir.auth.LoginRequest
import com.example.proyekakhir.auth.LoginResponse
import com.example.proyekakhir.MainActivity
import com.example.proyekakhir.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email & Password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email = email, password = password)
            ApiClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        val name = response.body()?.user?.name

                        Toast.makeText(this@LoginActivity, "Selamat datang $name", Toast.LENGTH_SHORT).show()

                        // Simpan token ke SharedPreferences
                        val shared = getSharedPreferences("APP", MODE_PRIVATE)
                        shared.edit().putString("TOKEN", token).apply()

                        // Pindah ke halaman utama
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Login gagal"
                        Toast.makeText(
                            this@LoginActivity,
                            "Login gagal: $errorMessage", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}