package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.User

data class EditProfileResponse(
    val success: Boolean,
    val user: User
)
