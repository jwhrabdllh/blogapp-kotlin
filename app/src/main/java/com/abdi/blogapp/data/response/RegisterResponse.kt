package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.User

data class RegisterResponse(
    val success: Boolean,
    val token: String,
    val user: User
)
