package com.example.proyekakhir.auth

import com.example.proyekakhir.model.User

data class LoginResponse(
    val user: User,
    val token: String
)