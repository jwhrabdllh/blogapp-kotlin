package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.Post

data class PostResponse(
    val success: Boolean,
    val post: Post
)
