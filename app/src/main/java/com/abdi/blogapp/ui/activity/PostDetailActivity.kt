package com.abdi.blogapp.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.model.Comment
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.ui.adapter.PostAdapter
import com.abdi.blogapp.utils.Constant
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostDetailActivity : AppCompatActivity() {
    private lateinit var ivPostPhoto: ImageView
    private lateinit var tvPostTitle: TextView
    private lateinit var tvPostDesc: TextView
    private lateinit var tvPostDate: TextView
    private lateinit var tvUsername: TextView
    private lateinit var ivUserPhoto: CircleImageView
    private lateinit var btnLike: ImageButton
    private lateinit var btnComment: ImageButton
    private lateinit var tvLikes: TextView
    private lateinit var tvComments: TextView
    private lateinit var btnPostOption: ImageButton
    private lateinit var sharedPref: SharedPreferences
    private lateinit var post: Post
    private var position: Int = -1
    private var commentsCount = 0

    companion object {
        const val EDIT_POST_REQUEST_CODE = 8
        const val COMMENT_POST_REQUEST_CODE = 9
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        init()
    }

    private fun init() {
        ivPostPhoto = findViewById(R.id.ivPostDetailPhoto)
        tvPostTitle = findViewById(R.id.tvPostDetailTitle)
        tvPostDesc = findViewById(R .id.tvPostDetailDesc)
        tvPostDate = findViewById(R.id.tvPostDetailDate)
        tvUsername = findViewById(R.id.tvPostDetailUser)
        ivUserPhoto = findViewById(R.id.ivPostDetailPhotoUser)
        btnLike = findViewById(R.id.btnPostDetailLike)
        btnComment = findViewById(R.id.btnPostDetailComment)
        tvLikes = findViewById(R.id.tvPostDetailLikes)
        tvComments = findViewById(R.id.tvPostDetailComments)
        btnPostOption = findViewById(R.id.btnPostDetailOption)
        sharedPref = application.getSharedPreferences("user", MODE_PRIVATE)

        loadPostData()

        commentsCount = post.comments

        tvLikes.setOnClickListener {
            val i = Intent(this, LikeActivity::class.java)
            i.putExtra("postId", post.id)
            i.putExtra("postPosition", position)
            startActivity(i)
        }

        btnLike.setImageResource(if (post.selfLike) R.drawable.ic_fav_red else R.drawable.ic_fav_border)

        btnLike.setOnClickListener {
            btnLike.setImageResource(
                if (post.selfLike) R.drawable.ic_fav_border else R.drawable.ic_fav_red
            )

            val postId = post.id
            val authorization = "Bearer ${sharedPref.getString("token", "") ?: run {
                Toast.makeText(this, "Session berakhir. Silahkan login kembali.", Toast.LENGTH_LONG).show()
                redirectToLogin()
                return@setOnClickListener
            }}"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiConfig.apiService.likePost(authorization, postId)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val likeResponse = response.body()
                            if (likeResponse != null && likeResponse.success) {
                                val newSelfLike = !post.selfLike
                                val newLikes = if (newSelfLike) post.likes + 1 else post.likes - 1
                                post.selfLike = newSelfLike
                                post.likes = newLikes

                                updateLikesView()
                            } else {
                                btnLike.setImageResource(
                                    if (post.selfLike) R.drawable.ic_fav_red else R.drawable.ic_fav_border
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PostDetailActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnComment.setOnClickListener {
            val i = Intent(this, CommentActivity::class.java)
            i.putExtra("postId", post.id)
            i.putExtra("postPosition", position)
            startActivityForResult(i, COMMENT_POST_REQUEST_CODE)
        }

        tvComments.setOnClickListener {
            val i = Intent(this, CommentActivity::class.java)
            i.putExtra("postId", post.id)
            i.putExtra("postPosition", position)
            startActivityForResult(i, COMMENT_POST_REQUEST_CODE)
        }

        if (post.user.id == sharedPref.getInt("id", 0)) {
            btnPostOption.visibility = View.VISIBLE
        } else {
            btnPostOption.visibility = View.GONE
        }

        btnPostOption.setOnClickListener {
            val popupMenu = PopupMenu(this, btnPostOption)
            popupMenu.inflate(R.menu.post_options_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.item_edit -> {
                        val i = Intent(this, EditPostActivity::class.java)
                        i.putExtra("postId", post.id)
                        i.putExtra("position", position)
                        i.putExtra("title", post.title)
                        i.putExtra("desc", post.desc)
                        startActivityForResult(i, EDIT_POST_REQUEST_CODE)
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
    }

    private fun deletePost(postId: Int, position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi")
        builder.setMessage("Apakah anda yakin?")
        builder.setPositiveButton("Hapus") { _, _ ->

            val token = sharedPref.getString("token", "") ?: ""
            if (token.isEmpty()) {
                Toast.makeText(this, "Session berakhir. Silahkan login kembali.", Toast.LENGTH_SHORT).show()
                redirectToLogin()
                return@setPositiveButton
            }
            val authorization = "Bearer $token"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiConfig.apiService.deletePost(postId, authorization)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val deleteResponse = response.body()
                            if (deleteResponse != null && deleteResponse.success) {
                                val intent = Intent()
                                intent.putExtra("position", position)
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this@PostDetailActivity, "Gagal menghapus post", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@PostDetailActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                }
            }
        }

        builder.setNegativeButton("Batal") { _, _ ->}
        builder.show()
    }

    @SuppressLint("SetTextI18n")
    private fun loadPostData() {
        post = intent.getParcelableExtra("post") ?: run {
            Log.e("PostDetailActivity", "Post Data Kosong")
            return
        }
        position = intent.getIntExtra("position", -1)

        Log.d("PostDetailActivity", "Post ID: ${post.id}, Position: $position")

        Picasso.get().load(Constant.BASE_URL + "storage/posts/" + post.photo).into(ivPostPhoto)
        Picasso.get().load(Constant.BASE_URL + "storage/profiles/" + post.user.photo).into(ivUserPhoto)
        tvPostTitle.text = post.title
        tvPostDesc.text = post.desc
        tvPostDate.text = post.date
        tvUsername.text = post.user.name + " " + post.user.lastname
        tvLikes.text = "${post.likes} suka"
        tvComments.text = "${post.comments} komentar"

        updateLikesView()
        updateCommentsView()
    }

    private fun updateLikesView() {
        tvLikes.text = "${post.likes} suka"
    }

    private fun updateCommentsView() {
        tvComments.text = "${commentsCount} komentar"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == COMMENT_POST_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val newComment = data?.getParcelableExtra<Comment>("newComment")
            val commentDeleted = data?.getBooleanExtra("commentDeleted", false) ?: false
            val deletedCommentId = data?.getIntExtra("deletedCommentId", -1) ?: -1

            if (newComment != null) {
                commentsCount += 1
                updateCommentsView()
            } else if (commentDeleted && deletedCommentId != -1) {
                commentsCount -= 1
                updateCommentsView()
            }
        }

        if (requestCode == EDIT_POST_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val updateTitle = data?.getStringExtra("editedTitle")
            val updateDesc = data?.getStringExtra("editedDesc")

            if (!updateTitle.isNullOrEmpty() && !updateDesc.isNullOrEmpty()) {
                post.title = updateTitle
                post.desc = updateDesc

                tvPostTitle.text = post.title
                tvPostDesc.text = post.desc
            }
        }
    }

    private fun redirectToLogin() {
        sharedPref.edit().remove("token").apply()

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun goBack(view: View) {
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        loadPostData()
    }
}