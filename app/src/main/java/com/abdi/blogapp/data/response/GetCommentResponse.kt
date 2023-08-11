package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.Comment

data class GetCommentResponse(
    val success: Boolean,
    val comments: List<Comment>
)
