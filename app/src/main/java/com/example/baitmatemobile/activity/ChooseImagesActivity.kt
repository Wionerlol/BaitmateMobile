package com.example.baitmatemobile.activity

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baitmatemobile.R
import com.example.baitmatemobile.adapter.ChooseImagesAdapter
import com.example.baitmatemobile.adapter.ImageItem
import com.google.android.material.button.MaterialButton

class ChooseImagesActivity : AppCompatActivity() {

    private lateinit var rvImages: RecyclerView
    private lateinit var btnNext: MaterialButton
    private lateinit var btnBack: MaterialButton

    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 100

    private val imageList = mutableListOf<ImageItem>()
    private lateinit var adapter: ChooseImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_images)

        rvImages = findViewById(R.id.rvImages)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), READ_EXTERNAL_STORAGE_REQUEST_CODE)
        } else {
            // 权限已授予，执行加载图片逻辑
            loadAllImagesFromDevice()
        }

        // 2. 设置 Adapter
        adapter = ChooseImagesAdapter(imageList) { position, isChecked ->
            updateNextButtonState()
        }
        rvImages.layoutManager = GridLayoutManager(this, 3)
        rvImages.adapter = adapter

        // 3. 点击下一步
        btnNext.setOnClickListener {
            val selectedImages = imageList.filter { it.isSelected }.map { it.uri }
            if (selectedImages.isNotEmpty()) {
                // 跳转到编辑图片页面
                val intent = Intent(this, UploadPostActivity::class.java)
                // 传递选中的图片URI
                intent.putParcelableArrayListExtra("selectedImages", ArrayList(selectedImages))
                startActivity(intent)
            } else {
            Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show()
        }

        }
    }

    private fun updateNextButtonState() {
        val hasSelected = imageList.any { it.isSelected }
        btnNext.isEnabled = hasSelected
    }

    private fun loadAllImagesFromDevice(): List<ImageItem> {
        // TODO: 从MediaStore或其他方式加载设备所有图片URI，填充imageList
        // 这里只是示例
        // imageList.add(ImageItem(uri, false))
        // ...
        // 指定要查询的 URI（外部存储中的图片）
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // 指定需要返回的列
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
            // 也可加上 DISPLAY_NAME、DATA 等字段
        )

        // 排序方式：根据添加日期倒序
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        // 通过 ContentResolver 查询
        contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            // 获取列索引
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            // val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED) // 如果需要日期的话

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                // 通过 id 动态拼接出每个图片对应的 URI
                val imageUri = ContentUris.withAppendedId(collection, id)

                // 加入列表
                imageList.add(ImageItem(uri = imageUri, isSelected = false))
            }
        }

        return imageList
    }


}
