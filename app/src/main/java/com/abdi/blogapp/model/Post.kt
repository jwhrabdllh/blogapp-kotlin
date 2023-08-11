package com.abdi.blogapp.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("likesCount") var likes: Int = 0,
    @SerializedName("selfLike") var selfLike: Boolean = false,
    @SerializedName("commentsCount") var comments: Int = 0,
    @SerializedName("title") var title: String = "",
    @SerializedName("desc") var desc: String = "",
    @SerializedName("photo") var photo: String? = null,
    @SerializedName("created_at") var date: String = "",
    @SerializedName("user") var user: User = User()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readParcelable(User::class.java.classLoader) ?: User()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(likes)
        parcel.writeByte(if (selfLike) 1 else 0)
        parcel.writeInt(comments)
        parcel.writeString(title)
        parcel.writeString(desc)
        parcel.writeString(photo)
        parcel.writeString(date)
        parcel.writeParcelable(user, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}