package com.abdi.blogapp.Model

data class Comment(
    var id: Int = 0,
    var comment: String = "",
    var date: String = "",
    var user: User = User()
)
