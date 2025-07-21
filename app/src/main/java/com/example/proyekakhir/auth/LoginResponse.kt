package com.example.proyekakhir.auth

import com.example.proyekakhir.auth.User

data class LoginResponse(
    val user: User,
    val token: String
)