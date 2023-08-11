package com.abdi.blogapp.ui.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.ui.activity.*
import com.abdi.blogapp.utils.Constant
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostAdapter(private val context: Context, private val list: ArrayList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostsHolder>() {

    private val sharedPref: SharedPreferences = context.applicationContext.getSharedPreferences("user", MODE_PRIVATE)

    companion object {
        const val POST_DETAIL_REQUEST_CODE = 7
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_posts, parent, false)
        return PostsHolder(view)
    }

    override fun onBindViewHolder(holder: PostsHolder, position: Int) {
        val post = list[position]
        Picasso.get().load(Constant.BASE_URL + "storage/profiles/" + post.user.photo)
            .into(holder.imgProfile)
        Picasso.get().load(Constant.BASE_URL + "storage/posts/" + post.photo).into(holder.imgPost)
        holder.tvName.text = post.user.name + " " + post.user.lastname
        holder.tvTitle.text = post.title
        holder.tvDate.text = post.date
        holder.tvComments.text = post.comments.toString()
        holder.tvLike.text = post.likes.toString()

        setDescription(holder, post)

        holder.itemView.setOnClickListener {
            val intent = Intent(context as HomeActivity, PostDetailActivity::class.java)
            intent.putExtra("post", post)
            intent.putExtra("position", position)
            context.startActivityForResult(intent, POST_DETAIL_REQUEST_CODE)
        }

        holder.btnLike.setOnClickListener {
            holder.btnLike.setImageResource(
                if (post.selfLike) R.drawable.ic_fav_border else R.drawable.ic_fav_red
            )

            val postId = post.id

            val token = sharedPref.getString("token", "") ?: ""
            if (token.isEmpty()) {
                Toast.makeText(context, "Session berakhir. Silahkan login kembali.", Toast.LENGTH_SHORT).show()
                redirectToLogin()
                return@setOnClickListener
            }
            val authorization = "Bearer $token"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiConfig.apiService.likePost(authorization, postId)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val likeResponse = response.body()
                            if (likeResponse != null && likeResponse.success) {
                                val mPost = list[position]
                                mPost.selfLike = !post.selfLike
                                mPost.likes = if (mPost.selfLike) post.likes + 1 else post.likes - 1
                                list[position] = mPost
                                notifyItemChanged(position)
                                notifyDataSetChanged()
                            } else {
                                holder.btnLike.setImageResource(
                                    if (post.selfLike) R.drawable.ic_fav_red else R.drawable.ic_fav_border
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        holder.btnComment.setOnClickListener {
            val i = Intent(context as HomeActivity, CommentActivity::class.java)
            i.putExtra("postId", post.id)
            i.putExtra("postPosition", position)
            context.startActivity(i)
        }

        holder.btnLike.setImageResource(
            if (post.selfLike) R.drawable.ic_fav_red else R.drawable.ic_fav_border
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class PostsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvPostName)
        val tvDate: TextView = itemView.findViewById(R.id.tvPostDate)
        val tvTitle: TextView = itemView.findViewById(R.id.tvPostTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvPostDesc)
        val tvLike: TextView = itemView.findViewById(R.id.tvPostLikes)
        val tvComments: TextView = itemView.findViewById(R.id.tvPostComments)
        val imgProfile: CircleImageView = itemView.findViewById(R.id.ivPostProfile)
        val imgPost: ImageView = itemView.findViewById(R.id.ivPostPhoto)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnPostLike)
        val btnComment: ImageButton = itemView.findViewById(R.id.btnPostComment)
    }

    private fun redirectToLogin() {
        sharedPref.edit()
            .remove("token")
            .apply()

        val intent = Intent(context, SignInActivity::class.java)
        context.startActivity(intent)
    }

    private fun setDescription(holder: PostsHolder, post: Post) {
        val maxDescLength = 125

        if (post.desc.length <= maxDescLength) {
            holder.tvDesc.text = post.desc
        } else {
            val shortDesc = post.desc.substring(0, maxDescLength) + " ..."
            holder.tvDesc.text = shortDesc
        }
    }
}
