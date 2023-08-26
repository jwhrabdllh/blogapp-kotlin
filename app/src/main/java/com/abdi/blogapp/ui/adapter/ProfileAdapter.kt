package com.abdi.blogapp.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.abdi.blogapp.R
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.ui.activity.PostDetailActivity
import com.abdi.blogapp.utils.Constant
import com.bumptech.glide.Glide

class ProfileAdapter(private val context: Context, private val arrayList: ArrayList<Post>) :
    RecyclerView.Adapter<ProfileAdapter.ProfilePostHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_profile_post, parent, false)
        return ProfilePostHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostHolder, position: Int) {
        val post: Post = arrayList[position]
        Glide.with(context)
            .load(Constant.BASE_URL + "storage/posts/" + post.photo)
            .into(holder.ivProfilePost)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("post", post)
            intent.putExtra("position", position)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    inner class ProfilePostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfilePost: ImageView = itemView.findViewById(R.id.ivMyPost)
    }
}
