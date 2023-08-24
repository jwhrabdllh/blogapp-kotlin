package com.abdi.blogapp.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.abdi.blogapp.ui.fragment.HomeFragment
import com.abdi.blogapp.model.Comment
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.ui.activity.CommentActivity
import com.abdi.blogapp.ui.activity.SignInActivity
import com.abdi.blogapp.ui.activity.UserProfileActivity
import com.abdi.blogapp.utils.Constant
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentAdapter(private val context: Context, private val list: ArrayList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentsHolder>() {

    private val sharedPref: SharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE)
    private val dialog: ProgressDialog = ProgressDialog(context)

    init {
        dialog.setCancelable(false)
    }

    inner class CommentsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: CircleImageView = itemView.findViewById(R.id.ivCommentUser)
        val tvName: TextView = itemView.findViewById(R.id.tvCommentName)
        val tvDate: TextView = itemView.findViewById(R.id.tvCommentDate)
        val tvComment: TextView = itemView.findViewById(R.id.tvCommentDesc)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_comment, parent, false)
        return CommentsHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CommentsHolder, position: Int) {
        val comment = list[position]
        Picasso.get().load(Constant.BASE_URL + "storage/profiles/" + comment.user.photo).into(holder.ivProfile)
        holder.tvName.text = comment.user.name + " " + comment.user.lastname
        holder.tvComment.text = comment.comment
        holder.tvDate.text = comment.date

        holder.tvName.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("userId", comment.user.id)
            context.startActivity(intent)
        }

        holder.ivProfile.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("userId", comment.user.id)
            context.startActivity(intent)
        }

        if (sharedPref.getInt("id", 0) != comment.user.id) {
            holder.btnDelete.visibility = View.GONE
        } else {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                AlertDialog.Builder(context)
                    .setMessage("Apakah anda yakin?")
                    .setPositiveButton("Hapus") { dialog, _ ->
                        deleteComment(comment.id, position)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun deleteComment(commentId: Int, position: Int) {
        dialog.setMessage("Loading")
        dialog.show()

        val token = sharedPref.getString("token", "")
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Session berakhir. Silahkan login kembali.", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }
        val authorization = "Bearer $token"


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.deleteComment(commentId, authorization)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        list.removeAt(position)

                        val postComment = HomeFragment.arrayList[CommentActivity.postPosition]
                        postComment.comments -= 1
                        HomeFragment.arrayList[CommentActivity.postPosition] = postComment
                        HomeFragment.recyclerView.adapter?.notifyDataSetChanged()
                        notifyDataSetChanged()

                        val resultIntent = Intent().apply {
                            putExtra("commentDeleted", true)
                            putExtra("deletedCommentId", commentId)
                        }
                        (context as CommentActivity).setResult(Activity.RESULT_OK, resultIntent)
                    }
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun redirectToLogin() {
        sharedPref.edit()
            .remove("token")
            .apply()

        val intent = Intent(context, SignInActivity::class.java)
        context.startActivity(intent)
        (context as CommentActivity).finish()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
