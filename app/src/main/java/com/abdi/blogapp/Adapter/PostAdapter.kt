package com.abdi.blogapp.Adapter

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
import com.abdi.blogapp.Model.Post
import com.abdi.blogapp.R
import com.abdi.blogapp.UI.CommentActivity
import com.abdi.blogapp.UI.EditPostActivity
import com.abdi.blogapp.UI.HomeActivity
import com.abdi.blogapp.Utils.ApiConfig
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONException
import org.json.JSONObject

class PostAdapter(private val context: Context, private val list: ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostsHolder>() {

    private val listAll = ArrayList<Post>(list)
    private val sharedPref: SharedPreferences = context.applicationContext.getSharedPreferences("user", MODE_PRIVATE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_posts, parent, false)
        return PostsHolder(view)
    }

    override fun onBindViewHolder(holder: PostsHolder, position: Int) {
        val post = list[position]
        Picasso.get().load(ApiConfig.URL + "storage/profiles/" + post.user.photo).into(holder.imgProfile)
        Picasso.get().load(ApiConfig.URL + "storage/posts/" + post.photo).into(holder.imgPost)
        holder.tvName.text = post.user.username
        holder.tvTitle.text = post.title
        holder.tvDate.text = post.date
        holder.tvDesc.text = post.desc
        holder.tvComments.text = "${post.comments} komentar"
        holder.tvLike.text = "${post.likes} suka"

        holder.btnLike.setImageResource(
            if (post.selfLike) R.drawable.ic_fav_red else R.drawable.ic_fav_border
        )

        // like click
        holder.btnLike.setOnClickListener {
            holder.btnLike.setImageResource(
                if (post.selfLike) R.drawable.ic_fav_border else R.drawable.ic_fav_red
            )

            val request = object : StringRequest(Method.POST, ApiConfig.LIKE_POST,
                Response.Listener { response ->
                    val mPost = list[position]
                    try {
                        val jsonObject = JSONObject(response)
                        if (jsonObject.getBoolean("success")) {
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
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { err -> err.printStackTrace() }) {

                // add token
                override fun getHeaders(): MutableMap<String, String> {
                    val token = sharedPref.getString("token", "") ?: ""
                    val map = HashMap<String, String>()
                    map["Authorization"] = "Bearer $token"
                    return map
                }

                override fun getParams(): MutableMap<String, String> {
                    val map = HashMap<String, String>()
                    map["id"] = post.id.toString()
                    return map
                }
            }

            request.retryPolicy = object : RetryPolicy {
                override fun getCurrentTimeout(): Int {
                    return 30000
                }

                override fun getCurrentRetryCount(): Int {
                    return 2
                }

                override fun retry(error: VolleyError) {}
            }

            val queue = Volley.newRequestQueue(context)
            queue.add(request)
        }

        if (post.user.id == sharedPref.getInt("id", 0)) {
            holder.btnPostOption.visibility = View.VISIBLE
        } else {
            holder.btnPostOption.visibility = View.GONE
        }

        holder.btnPostOption.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.btnPostOption)
            popupMenu.inflate(R.menu.post_options_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.item_edit -> {
                        val i = Intent(context as HomeActivity, EditPostActivity::class.java)
                        i.putExtra("postId", post.id)
                        i.putExtra("position", position)
                        i.putExtra("title", post.title)
                        i.putExtra("desc", post.desc)
                        context.startActivity(i)
                        true
                    }
                    R.id.item_delete -> {
                        deletePost(post.id, position)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        holder.tvComments.setOnClickListener {
            val i = Intent(context as HomeActivity, CommentActivity::class.java)
            i.putExtra("postId", post.id)
            i.putExtra("postPosition", position)
            context.startActivity(i)
        }

        holder.btnComment.setOnClickListener {
            val i = Intent(context as HomeActivity, CommentActivity::class.java)
            i.putExtra("postId", post.id)
            i.putExtra("postPosition", position)
            context.startActivity(i)
        }
    }

    // delete post
    private fun deletePost(postId: Int, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Konfirmasi")
        builder.setMessage("Apakah anda yakin?")
        builder.setPositiveButton("Hapus") { _, _ ->
            val request = object : StringRequest(Method.DELETE, ApiConfig.DELETE_POST + postId,
                Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        if (jsonObject.getBoolean("success")) {
                            list.removeAt(position)
                            notifyItemRemoved(position)
                            notifyDataSetChanged()
                            listAll.clear()
                            listAll.addAll(list)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener {}) {

                override fun getHeaders(): MutableMap<String, String> {
                    val token = sharedPref.getString("token", "") ?: ""
                    val map = HashMap<String, String>()
                    map["Authorization"] = "Bearer $token"
                    return map
                }

                override fun getParams(): MutableMap<String, String> {
                    val map = HashMap<String, String>()
                    map["id"] = postId.toString()
                    return map
                }
            }

            request.retryPolicy = object : RetryPolicy {
                override fun getCurrentTimeout(): Int {
                    return 50000
                }

                override fun getCurrentRetryCount(): Int {
                    return 50000
                }

                override fun retry(error: VolleyError) {}
            }

            val queue = Volley.newRequestQueue(context)
            queue.add(request)
        }

        builder.setNegativeButton("Batal") { _, _ -> }
        builder.show()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private val filter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList = ArrayList<Post>()
            if (constraint.toString().isEmpty()) {
                filteredList.addAll(listAll)
            } else {
                for (post in listAll) {
                    if (post.title.toLowerCase().contains(constraint.toString().toLowerCase())
                        || post.user.username.toLowerCase().contains(constraint.toString().toLowerCase())
                    ) {
                        filteredList.add(post)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            list.clear()
            list.addAll(results.values as Collection<Post>)
            notifyDataSetChanged()
        }
    }

    fun getFilter(): Filter {
        return filter
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
        val btnPostOption: ImageButton = itemView.findViewById(R.id.btnPostOption)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnPostLike)
        val btnComment: ImageButton = itemView.findViewById(R.id.btnPostComment)

        init {
            btnPostOption.visibility = View.GONE
        }
    }
}
