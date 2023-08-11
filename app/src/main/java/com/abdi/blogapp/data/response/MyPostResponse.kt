package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.Post
import com.abdi.blogapp.model.User

data class MyPostResponse(
    val success: Boolean,
    val posts: List<Post>,
    val user: User
)
