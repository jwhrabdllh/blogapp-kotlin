package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.User

data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val user: User?
)
