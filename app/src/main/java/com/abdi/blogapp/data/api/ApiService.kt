package com.abdi.blogapp.data.api

import com.abdi.blogapp.data.response.*
import com.abdi.blogapp.utils.Constant
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST(Constant.LOGIN)
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST(Constant.REGISTER)
    suspend fun register(
        @Field("name") name: String,
        @Field("lastname") lastname: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<RegisterResponse>

    @FormUrlEncoded
    @POST(Constant.SIGN_UP_PHOTO)
    suspend fun savePhotoProfile(
        @Header("Authorization") token: String,
        @Field("photo") photo: String
    ): Response<SavePhotoResponse>

    @FormUrlEncoded
    @POST(Constant.USER_PROFILE)
    suspend fun editProfile(
        @Header("Authorization") token: String,
        @Field("name") name: String,
        @Field("lastname") lastname: String,
        @Field("email") email: String,
        @Field("photo") photo: String
    ): Response<EditProfileResponse>

    @FormUrlEncoded
    @POST(Constant.ADD_POST)
    suspend fun addPost(
        @Header("Authorization") token: String,
        @Field("photo") photo: String,
        @Field("title") title: String,
        @Field("desc") desc: String
    ): Response<PostResponse>

    @FormUrlEncoded
    @PUT(Constant.UPDATE_POST + "{id}")
    suspend fun editPost(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Field("title") title: String,
        @Field("desc") desc: String
    ): Response<EditPostResponse>

    @GET(Constant.MY_POST)
    suspend fun myPost(
        @Header("Authorization") token: String
    ): Response<MyPostResponse>

    @GET(Constant.LOGOUT)
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>

    @GET(Constant.GET_PAGINATION)
    fun getPagination(
        @Header("Authorization") token: String,
        @QueryMap params: HashMap<String, String>
    ): Call<PostsPaginationResponse>

    @DELETE(Constant.DELETE_POST + "{id}")
    suspend fun deletePost(
        @Path("id") postId: Int,
        @Header("Authorization") token: String
    ): Response<DeletePostResponse>

    @FormUrlEncoded
    @POST(Constant.LIKE_POST)
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Field("id") postId: Int
    ): Response<LikeResponse>

    @GET(Constant.GET_LIKES + "{id}")
    suspend fun getLikes(
        @Path("id") postId: Int,
        @Header("Authorization") token: String
    ): Response<GetLikeResponse>

    @FormUrlEncoded
    @POST(Constant.CREATE_COMMENT)
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Field("id") postId: Int,
        @Field("comment") comment: String
    ): Response<CommentResponse>

    @GET(Constant.GET_COMMENTS + "{id}")
    suspend fun getComment(
        @Path("id") postId: Int,
        @Header("Authorization") token: String
    ): Response<GetCommentResponse>

    @DELETE(Constant.DELETE_COMMENT + "{id}")
    suspend fun deleteComment(
        @Path("id") postId: Int,
        @Header("Authorization") token: String
    ): Response<DelCommentResponse>
}