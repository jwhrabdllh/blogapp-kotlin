package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.Comment

data class CommentResponse(
    val success: Boolean,
    val comment: Comment
)
