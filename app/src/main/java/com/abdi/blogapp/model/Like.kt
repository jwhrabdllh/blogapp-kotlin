package com.abdi.blogapp.model

import com.google.gson.annotations.SerializedName

data class Like(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("created_at") var date: String = "",
    @SerializedName("user") var user: User = User()
)