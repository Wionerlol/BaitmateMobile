package com.example.baitmatemobile.activity

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baitmatemobile.R
import com.example.baitmatemobile.adapter.PreviewImagesAdapter
import com.example.baitmatemobile.model.CreatedPostDTO
import com.example.baitmatemobile.model.Image
import com.example.baitmatemobile.model.Post
import com.example.baitmatemobile.model.User
import com.example.baitmatemobile.network.ApiService
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch

class UploadPostActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var rvPreviewImages: RecyclerView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var spinnerLocation: Spinner
    private lateinit var btnPublish: Button

    private lateinit var sharedPrefs: SharedPreferences

    private val editedUris = mutableListOf<Uri>()
    private lateinit var previewAdapter: PreviewImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_post)

        sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        btnBack = findViewById(R.id.btnBack)
        rvPreviewImages = findViewById(R.id.rvPreviewImages)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        spinnerLocation = findViewById(R.id.spinnerLocation)
        btnPublish = findViewById(R.id.btnPublish)

        // 1. 获取从编辑页面传来的图片
        val uris = intent.getParcelableArrayListExtra<Uri>("selectedImages")
        if (uris != null) {
            editedUris.addAll(uris)
        }
        val imageByteArrays = mutableListOf<ByteArray>()
        uris?.forEach { uri ->
            val byteArray = uriToByteArray(this, uri)
            byteArray?.let { imageByteArrays.add(it) }
        }
        val imageBase64List = imageByteArrays.map { byteArrayToBase64(it) }


        // 2. 设置 RecyclerView 预览选中图片
        previewAdapter = PreviewImagesAdapter(editedUris)
        rvPreviewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPreviewImages.adapter = previewAdapter

        // 3. 初始化地点下拉框
        val locations = listOf("", "West coast park", "East coast park")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLocation.adapter = adapterSpinner

        // 4. 返回按钮
        btnBack.setOnClickListener {
            finish() // 返回上一页
        }

        // 5. 发布按钮点击 -> 调用后端API
        btnPublish.setOnClickListener {
            publishPost(imageBase64List)
        }
    }

    private fun publishPost(imageBase64: List<String>) {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val locationSelected = spinnerLocation.selectedItem.toString()

        var userId = sharedPrefs.getLong("userId",0)

        // 协程或回调方式请求
        lifecycleScope.launch {
            try {
                val checkImageResults = mutableListOf<String>()
                for(image in imageBase64){
                    val request = ImageCheckRequest(image)
                    val response = RetrofitClient.instance.checkImage(request)
                    if (response.isSuccessful) {
                        val status = response.body()?.status
                        if (status != null) {
                            checkImageResults.add(status)
                        } else {
                            throw Exception("Empty response body")
                        }
                    } else {
                        throw Exception("Image check failed: ${response.code()}")
                    }
                }
                val post = CreatedPostDTO(
                    postTitle = title,
                    postContent = content,
                    userId = userId,
                    location = if (locationSelected.isEmpty()) null else locationSelected,
                    status = if (checkImageResults.contains("pending")) "pending1" else "approved",
                    imageBase64List = imageBase64
                )
                val createdPost = RetrofitClient.instance.createPost(post)
                // 请求成功，跳转回Home Fragment
                // 常见做法是：finish()，如果是单Activity+多Fragment架构，可以让Activity去切换到HomeFragment
                Toast.makeText(this@UploadPostActivity, "UPLOAD SUCCESSFUL！ID=${createdPost}", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@UploadPostActivity, "FAILED: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    data class ImageCheckRequest(val image: String)
    data class ImageCheckResponse(
        val status: String
    )

    private fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun byteArrayToBase64(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

}
