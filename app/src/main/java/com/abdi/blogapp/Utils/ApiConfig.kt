package com.abdi.blogapp.Utils

object ApiConfig {
    const val URL = "http://192.168.231.225/"
    const val HOME = "${URL}api"
    const val REGISTER = "$HOME/register"
    const val LOGIN = "$HOME/login"
    const val LOGOUT = "$HOME/logout"
    const val ADD_SIGN_UP_PHOTO = "$HOME/add_photo_screen"
    const val USER_PROFILE = "$HOME/user_profile"
    const val POST = "$HOME/post"
    const val GET_POST = "$POST/get_posts"
    const val ADD_POST = "$POST/create"
    const val UPDATE_POST = "$POST/update"
    const val LIKE_POST = "$POST/like"
    const val DELETE_POST = "$POST/delete/"
    const val COMMENT = "$POST/comment"
    const val CREATE_COMMENT = "$HOME/comment/create"
    const val DELETE_COMMENT = "$HOME/comment/delete/"
    const val MY_POST = "$POST/my_post"
}