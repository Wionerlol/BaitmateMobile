package com.example.baitmatemobile.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.baitmatemobile.R
import com.example.baitmatemobile.adapter.CommentAdapter
import com.example.baitmatemobile.adapter.ImagePagerAdapter
import com.example.baitmatemobile.adapter.lifecycleOwner
import com.example.baitmatemobile.fragment.OthersProfileFragment
import com.example.baitmatemobile.fragment.ProfileFragment
import com.example.baitmatemobile.model.CreateCommentDTO
import com.example.baitmatemobile.model.Post
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch

class PostDetailActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var ivBack: ImageView
    private lateinit var viewPagerImages: ViewPager2
    private lateinit var tvPostTitle: TextView
    private lateinit var tvPostContent: TextView
    private lateinit var rvComments: RecyclerView
    private lateinit var tvPostInfo: TextView
    private lateinit var btnFollow: Button
    private lateinit var etComment: EditText
    private lateinit var ivComment: ImageView
    private lateinit var ivSave: ImageView
    private lateinit var tvSaveCount: TextView

    private var postId: Long? = null
    private var userId: Long =-1
    private var post: Post? = null
    private val commentAdapter = CommentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        initViews()
        postId = intent.getLongExtra("postId", -1)
        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPrefs.getLong("userId", -1)

        ivBack.setOnClickListener { onBackPressed() }
        btnFollow.setOnClickListener { toggleFollow() }

        if (postId == null || postId == -1L) {
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPostDetails(postId!!)
        lifecycleScope.launch {
            try {
                val latestPost = RetrofitClient.instance.getPostByIdWithUser(postId ?: return@launch, userId)
                val isSaved = latestPost.savedByCurrentUser
                val newCount = latestPost.savedCount ?: 0

                if (isSaved) {
                    ivSave.setImageResource(R.drawable.ic_save_filled)
                } else {
                    ivSave.setImageResource(R.drawable.ic_save_outline)
                }
                tvSaveCount.text = newCount.toString()
                ivSave.tag = isSaved

                // **同步更新 post 数据**
                post!!.likeCount = newCount
                post!!.savedByCurrentUser = isSaved

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        ivSave.setOnClickListener { toggleSave(postId!!,userId) }

        ivComment.setOnClickListener { submitComment() }


    }

    private fun initViews() {
        tvUsername = findViewById(R.id.tvUsername)
        tvUsername.isEnabled = false
        ivBack = findViewById(R.id.ivBack)
        viewPagerImages = findViewById(R.id.viewPagerImages)
        tvPostTitle = findViewById(R.id.tvPostTitle)
        tvPostContent = findViewById(R.id.tvPostContent)
        tvPostInfo = findViewById(R.id.tvPostInfo)
        btnFollow = findViewById(R.id.btnFollow)
        rvComments = findViewById(R.id.rvComments)
        etComment = findViewById(R.id.etComment)
        ivComment = findViewById(R.id.ivComment)
        ivSave = findViewById(R.id.ivSave)
        tvSaveCount = findViewById(R.id.tvSaveCount)

        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter
    }

    private fun loadPostDetails(id: Long) {
        lifecycleScope.launch {
            try {
                val fetchedPost = RetrofitClient.instance.getPostById(id)
                post = fetchedPost
                bindPostData(fetchedPost)
                tvUsername.isEnabled = true
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PostDetailActivity, "loadPostDetails failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindPostData(post: Post) {
        tvUsername.text = post.user?.username ?: "Unknown"
        tvPostTitle.text = post.postTitle
        tvPostContent.text = post.postContent
        tvPostInfo.text = "Posted on ${post.postTime} at ${post.location}"

        val images = post.images ?: emptyList()
        val imageAdapter = ImagePagerAdapter(images)
        viewPagerImages.adapter = imageAdapter

        val comments = post.comments ?: emptyList()
        commentAdapter.submitList(comments)

        tvSaveCount.text = post.savedCount.toString()

        val isSaved = post.savedByCurrentUser
        val newCount = post.savedCount ?: 0

        if (isSaved) {
            ivSave.setImageResource(R.drawable.ic_save_filled)
        } else {
            ivSave.setImageResource(R.drawable.ic_save_outline)
        }
        tvSaveCount.text = newCount.toString()
        ivSave.tag = isSaved
        post.likeCount = newCount
        post.savedByCurrentUser = isSaved

        tvUsername.setOnClickListener {
            val viewedUserId = post.user?.id
            if (viewedUserId == null) {
                Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (viewedUserId == userId) {
                val profileFragment = ProfileFragment()
                supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, profileFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                val othersProfileFragment = OthersProfileFragment.newInstance(viewedUserId)
                supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, othersProfileFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun toggleFollow() {
        // 关注逻辑
        Toast.makeText(this, "Follow clicked", Toast.LENGTH_SHORT).show()
    }

    private fun toggleSave(postId: Long, userId: Long) {
        lifecycleScope.launch {
            try {
                val updatedPost = RetrofitClient.instance.toggleSave(
                    postId = postId ?: return@launch,
                    userId = userId
                )

                val newCount = updatedPost.savedCount ?: 0

                val isSaved = updatedPost.savedByCurrentUser

                updateSaveUI(isSaved, newCount)

                post!!.savedCount = newCount

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun updateSaveUI(isSaved: Boolean, newCount: Int) {

        if (isSaved) {
            ivSave.setImageResource(R.drawable.ic_save_filled)
        } else {
            ivSave.setImageResource(R.drawable.ic_save_outline)
        }
        tvSaveCount.text = newCount.toString()
        ivSave.tag = isSaved
    }

    private fun submitComment() {

        val commentText = etComment.text.toString().trim()

        if (commentText.isEmpty()) {
            Toast.makeText(this, "can't submit empty comment", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getLong("userId", -1)

        val comment = CreateCommentDTO(
            comment = commentText,
            postId = postId,
            userId = userId
        )

        lifecycleScope.launch {
            try {
                val createdComment = RetrofitClient.instance.createComment(comment)
                Toast.makeText(this@PostDetailActivity, "UPLOAD SUCCESSFUL！ID=${createdComment}", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PostDetailActivity, "FAILED: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

