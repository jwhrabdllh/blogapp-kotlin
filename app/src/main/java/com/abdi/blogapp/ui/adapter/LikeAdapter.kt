package com.abdi.blogapp.ui.adapter

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abdi.blogapp.R
import com.abdi.blogapp.model.Like
import com.abdi.blogapp.utils.Constant
import com.squareup.picasso.Picasso
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
        Picasso.get().load(Constant.BASE_URL + "storage/profiles/" + like.user.photo).into(holder.ivPhotoUser)
        holder.tvNameUser.text = like.user.name + " " + like.user.lastname
        holder.tvDate.text = like.date
    }

    override fun getItemCount(): Int {
        return list.size
    }
}