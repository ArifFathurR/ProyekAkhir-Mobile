package com.example.proyekakhir.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyekakhir.ApiClient
import com.example.proyekakhir.MainActivity
import com.example.proyekakhir.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Cek apakah user sudah login sebelumnya
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        val token = shared.getString("TOKEN", null)

        if (!token.isNullOrEmpty()) {
            // Token masih ada, langsung ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Lanjut ke tampilan login
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
                        val loginResponse = response.body()
                        val token = loginResponse?.token
                        val user = loginResponse?.user

                        if (token != null && user != null) {
                            // Simpan data user & token ke SharedPreferences
                            shared.edit()
                                .putString("TOKEN", token)
                                .putInt("USER_ID", user.id)
                                .putString("USER_NAME", user.name)
                                .putString("USER_EMAIL", user.email)
                                .apply()

                            Toast.makeText(this@LoginActivity, "Selamat datang ${user.name}", Toast.LENGTH_SHORT).show()

                            // Pindah ke halaman utama
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Login gagal: Data tidak lengkap", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Login gagal"
                        Toast.makeText(this@LoginActivity, "Login gagal: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
