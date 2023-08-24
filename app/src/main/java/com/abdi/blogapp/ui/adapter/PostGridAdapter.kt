package com.abdi.blogapp.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.R
import com.abdi.blogapp.ui.activity.*
import com.abdi.blogapp.utils.Constant
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostGridAdapter(private val context: Context, private val list: ArrayList<Post>) :
    RecyclerView.Adapter<PostGridAdapter.PostsHolder>() {

    companion object {
        const val POST_DETAIL_REQUEST_CODE = 7
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_posts_grid, parent, false)
        return PostsHolder(view)
    }

    override fun onBindViewHolder(holder: PostsHolder, position: Int) {
        val post = list[position]
        Picasso.get().load(Constant.BASE_URL + "storage/profiles/" + post.user.photo).into(holder.imgProfile)
        Picasso.get().load(Constant.BASE_URL + "storage/posts/" + post.photo).into(holder.imgPost)
        holder.tvName.text = post.user.name + " " + post.user.lastname
        holder.tvComments.text = post.comments.toString()
        holder.tvLike.text = post.likes.toString()

        holder.itemView.setOnClickListener {
            val intent = Intent(context as HomeActivity, PostDetailActivity::class.java)
            intent.putExtra("post", post)
            intent.putExtra("position", position)
            context.startActivityForResult(intent, POST_DETAIL_REQUEST_CODE)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class PostsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvPostGridName)
        val tvLike: TextView = itemView.findViewById(R.id.tvPostGridLikes)
        val tvComments: TextView = itemView.findViewById(R.id.tvPostGridComments)
        val imgProfile: CircleImageView = itemView.findViewById(R.id.ivPostGridProfile)
        val imgPost: ImageView = itemView.findViewById(R.id.ivPostGridPhoto)
    }
}
