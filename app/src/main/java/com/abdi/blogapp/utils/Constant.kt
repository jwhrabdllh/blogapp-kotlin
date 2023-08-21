package com.abdi.blogapp.utils

object Constant {
    const val BASE_URL = "http://44.211.114.58/"
    const val HOME = "${BASE_URL}api"
    const val REGISTER = "$HOME/register"
    const val LOGIN = "$HOME/login"
    const val LOGOUT = "$HOME/logout"
    const val SIGN_UP_PHOTO = "$HOME/add_photo_screen"
    const val USER_PROFILE = "$HOME/user_profile"
    const val POST = "$HOME/post"
    const val GET_PAGINATION = "$POST/get_pagination"
    const val ADD_POST = "$POST/create"
    const val UPDATE_POST = "$POST/update/"
    const val LIKE_POST = "$POST/like"
    const val GET_LIKES = "$POST/user_like/"
    const val DELETE_POST = "$POST/delete/"
    const val GET_COMMENTS = "$POST/comment/"
    const val CREATE_COMMENT = "$HOME/comment/create"
    const val DELETE_COMMENT = "$HOME/comment/delete/"
    const val MY_POST = "$POST/my_post"
}