package com.abdi.blogapp.Model

data class Post(
    var id: Int = 0,
    var likes: Int = 0,
    var comments: Int = 0,
    var date: String = "",
    var title: String = "",
    var desc: String = "",
    var photo: String = "",
    var user: User = User(),
    var selfLike: Boolean = false
)
