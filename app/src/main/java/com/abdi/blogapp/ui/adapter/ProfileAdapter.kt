package com.abdi.blogapp.ui.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.abdi.blogapp.R
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.ui.activity.HomeActivity
import com.abdi.blogapp.ui.activity.PostDetailActivity
import com.abdi.blogapp.utils.Constant
import com.squareup.picasso.Picasso

class ProfileAdapter(private val context: Context, private val arrayList: ArrayList<Post>) :
    RecyclerView.Adapter<ProfileAdapter.ProfilePostHolder>() {

    companion object {
        const val MY_POST_REQUEST_CODE = 9
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_profile_post, parent, false)
        return ProfilePostHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostHolder, position: Int) {
        val post: Post = arrayList[position]

        Picasso.get().load(Constant.BASE_URL + "storage/posts/" + post.photo).into(holder.ivProfilePost)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("post", post)
            intent.putExtra("position", position)
            (context as HomeActivity).startActivityForResult(intent, MY_POST_REQUEST_CODE)
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    inner class ProfilePostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfilePost: ImageView = itemView.findViewById(R.id.ivMyPost)
    }
}
