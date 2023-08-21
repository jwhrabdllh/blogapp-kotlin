package com.abdi.blogapp.data.response

import com.abdi.blogapp.model.Post
import com.google.gson.annotations.SerializedName

data class PostsPaginationResponse(
    val page: Int,
    @SerializedName("total_pages") val totalPages: Int,
    val data: ArrayList<Post>
)
