package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.Post
import com.abdi.blogapp.model.User

data class UserProfileResponse(
    val success: Boolean,
    val user: User,
    val posts: ArrayList<Post>,
)
