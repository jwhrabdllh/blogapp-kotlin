package com.abdi.blogapp.ui.adapter

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abdi.blogapp.R
import com.abdi.blogapp.model.Like
import com.abdi.blogapp.ui.activity.UserProfileActivity
import com.abdi.blogapp.utils.Constant
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class LikeAdapter(private val context: Context, private val list: ArrayList<Like>) :
    RecyclerView.Adapter<LikeAdapter.LikesHolder>() {

    inner class LikesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhotoUser: CircleImageView = itemView.findViewById(R.id.ivLikeUser)
        val tvNameUser: TextView = itemView.findViewById(R.id.tvLikeName)
        val tvDate: TextView = itemView.findViewById(R.id.tvLikeDate)
    }

    private val dialog: ProgressDialog = ProgressDialog(context)

    init {
        dialog.setCancelable(false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeAdapter.LikesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_like, parent, false)
        return LikesHolder(view)
    }

    override fun onBindViewHolder(holder: LikeAdapter.LikesHolder, position: Int) {
        val like = list[position]
        Glide.with(context)
            .load(Constant.BASE_URL + "storage/profiles/" + like.user.photo)
            .into(holder.ivPhotoUser)
        holder.tvNameUser.text = like.user.name + " " + like.user.lastname
        holder.tvDate.text = like.date

        holder.tvNameUser.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("userId", like.user.id)
            context.startActivity(intent)
        }

        holder.ivPhotoUser.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("userId", like.user.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}