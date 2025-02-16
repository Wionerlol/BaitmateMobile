package com.example.baitmatemobile.fragment

import android.os.Bundle
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.baitmatemobile.R
import com.example.baitmatemobile.adapter.CommentAdapter
import com.example.baitmatemobile.adapter.ImagePagerAdapter
import com.example.baitmatemobile.databinding.FragmentPostDetailBinding
import com.example.baitmatemobile.model.CreateCommentDTO
import com.example.baitmatemobile.model.Post
import com.example.baitmatemobile.model.PostReportRequest
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch

class PostDetailFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var viewPagerImages: ViewPager2
    private lateinit var tvPostTitle: TextView
    private lateinit var tvPostContent: TextView
    private lateinit var rvComments: RecyclerView
    private lateinit var tvPostInfo: TextView
    private lateinit var etComment: EditText
    private lateinit var ivComment: ImageView
    private lateinit var ivSave: ImageView
    private lateinit var tvSaveCount: TextView
    private lateinit var ivReport: ImageView

    private var postId: Long? = null
    private var userId: Long = -1
    private var post: Post? = null
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPostDetailBinding =
            FragmentPostDetailBinding.inflate(inflater, container, false)
        binding.setFragment(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPrefs.getLong("userId", -1)

        commentAdapter = CommentAdapter(viewLifecycleOwner, userId)
        initViews(view)

        postId = arguments?.getLong("postId") ?: -1

        if (postId == null || postId == -1L) {
            Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        loadPostDetails(postId!!)

        ivSave.setOnClickListener { toggleSave(postId!!, userId) }
        ivComment.setOnClickListener { submitComment() }
        ivReport.setOnClickListener { report(postId!!)}
    }

    private fun report(postId: Long) {
        lifecycleScope.launch {
            try {
                val request = PostReportRequest(postId)
                RetrofitClient.instance.reportPost(request)
                Toast.makeText(requireContext(), "REPORT SUCCESSFUL！", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "report failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews(view: View) {
        tvUsername = view.findViewById(R.id.tvUsername)
        tvUsername.isEnabled = false
        viewPagerImages = view.findViewById(R.id.viewPagerImages)
        tvPostTitle = view.findViewById(R.id.tvPostTitle)
        tvPostContent = view.findViewById(R.id.tvPostContent)
        tvPostInfo = view.findViewById(R.id.tvPostInfo)
        rvComments = view.findViewById(R.id.rvComments)
        etComment = view.findViewById(R.id.etComment)
        ivComment = view.findViewById(R.id.ivComment)
        ivSave = view.findViewById(R.id.ivSave)
        tvSaveCount = view.findViewById(R.id.tvSaveCount)
        ivReport = view.findViewById(R.id.ivReport)

        rvComments.layoutManager = LinearLayoutManager(requireContext())
        rvComments.adapter = commentAdapter
    }

    private fun loadPostDetails(id: Long) {
        lifecycleScope.launch {
            try {
                val fetchedPost = RetrofitClient.instance.getPostByIdWithUser(id,userId)
                post = fetchedPost
                bindPostData(fetchedPost)
                tvUsername.isEnabled = true
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "loadPostDetails failed", Toast.LENGTH_SHORT).show()
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

        ivSave.setImageResource(if (post.savedByCurrentUser == true) R.drawable.ic_save_filled else R.drawable.ic_save_outline)
        ivSave.tag = post.savedByCurrentUser

        tvUsername.setOnClickListener {
            val viewedUserId = post.user?.id
            Log.d("Profile Fragment", "${post.user?.id}")
            if (viewedUserId == null) {
                Toast.makeText(requireContext(), "User ID is null", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (viewedUserId == userId) {
                parentFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, ProfileFragment())
                    .addToBackStack("post_detail_to_profile")
                    .commit()
            } else {
                parentFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, OthersProfileFragment.newInstance(viewedUserId))
                    .addToBackStack("post_detail_to_profile")
                    .commit()
            }
        }
    }

    private fun toggleSave(postId: Long, userId: Long) {
        lifecycleScope.launch {
            try {
                val updatedPost = RetrofitClient.instance.toggleSave(postId, userId)
                val newCount = updatedPost.savedCount ?: 0
                //val isSaved = updatedPost.savedByCurrentUser
                val isSaved = !((ivSave.tag as? Boolean) ?: false)
                updateSaveUI(isSaved, newCount)
                post?.savedCount = newCount
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateSaveUI(isSaved: Boolean, newCount: Int) {
        ivSave.setImageResource(if (isSaved) R.drawable.ic_save_filled else R.drawable.ic_save_outline)
        tvSaveCount.text = newCount.toString()
        ivSave.tag = isSaved
    }

    private fun submitComment() {
        val commentText = etComment.text.toString().trim()

        if (commentText.isEmpty()) {
            Toast.makeText(requireContext(), "Can't submit empty comment", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getLong("userId", -1)

        val comment = CreateCommentDTO(
            comment = commentText,
            postId = postId,
            userId = userId
        )

        lifecycleScope.launch {
            try {
                val createdComment = RetrofitClient.instance.createComment(comment)
                Toast.makeText(requireContext(), "UPLOAD SUCCESSFUL！ID=${createdComment}", Toast.LENGTH_SHORT).show()
                postId?.let { loadPostDetails(it) }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "FAILED: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
