package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.Like

data class GetLikeResponse(
    val success: Boolean,
    val likes: List<Like>
)
