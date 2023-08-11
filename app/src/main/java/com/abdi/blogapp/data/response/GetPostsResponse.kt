package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.Post

data class GetPostsResponse(
    val success: Boolean,
    val posts: List<Post>
)
