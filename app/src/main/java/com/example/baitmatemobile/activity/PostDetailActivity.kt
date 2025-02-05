package com.example.baitmatemobile.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.baitmatemobile.R
import com.example.baitmatemobile.adapter.CommentAdapter
import com.example.baitmatemobile.adapter.ImagePagerAdapter
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

    private var postId: Long? = null
    private var post: Post? = null
    private val commentAdapter = CommentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        initViews()
        // 获取传递过来的 postId
        postId = intent.getLongExtra("postId", -1)

        ivBack.setOnClickListener { onBackPressed() }

        if (postId == null || postId == -1L) {
            // 参数错误
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPostDetails(postId!!)
    }

    private fun initViews() {
        tvUsername = findViewById(R.id.tvUsername)
        ivBack = findViewById(R.id.ivBack)
        viewPagerImages = findViewById(R.id.viewPagerImages)
        tvPostTitle = findViewById(R.id.tvPostTitle)
        tvPostContent = findViewById(R.id.tvPostContent)
        rvComments = findViewById(R.id.rvComments)

        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter
    }

    private fun loadPostDetails(id: Long) {
        lifecycleScope.launch {
            try {
                val fetchedPost = RetrofitClient.instance.getPostById(id)
                post = fetchedPost
                bindPostData(fetchedPost)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PostDetailActivity, "加载帖子详情失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindPostData(post: Post) {
        tvUsername.text = post.user?.username ?: "Unknown"
        tvPostTitle.text = post.postTitle
        tvPostContent.text = post.postContent

        // 图片
        val images = post.images ?: emptyList()
        val imageAdapter = ImagePagerAdapter(images)
        viewPagerImages.adapter = imageAdapter

        // 评论
        val comments = post.comments ?: emptyList()
        commentAdapter.submitList(comments)
    }
}
