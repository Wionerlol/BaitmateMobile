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
            loadAllImagesFromDevice()
        }

        adapter = ChooseImagesAdapter(imageList) { position, isChecked ->
            updateNextButtonState()
        }
        rvImages.layoutManager = GridLayoutManager(this, 3)
        rvImages.adapter = adapter

        btnNext.setOnClickListener {
            val selectedImages = imageList.filter { it.isSelected }.map { it.uri }
            if (selectedImages.isNotEmpty()) {
                val intent = Intent(this, UploadPostActivity::class.java)
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

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            // val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val imageUri = ContentUris.withAppendedId(collection, id)

                imageList.add(ImageItem(uri = imageUri, isSelected = false))
            }
        }

        return imageList
    }


}
