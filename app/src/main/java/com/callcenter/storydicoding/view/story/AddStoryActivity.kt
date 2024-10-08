package com.callcenter.storydicoding.view.story

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.callcenter.storydicoding.R
import com.callcenter.storydicoding.data.model.AddStoryResponse
import com.callcenter.storydicoding.data.network.ApiClient
import com.callcenter.storydicoding.data.pref.UserPreference
import com.callcenter.storydicoding.data.viewmodel.StoryViewModel
import com.callcenter.storydicoding.data.viewmodel.StoryViewModelFactory
import com.callcenter.storydicoding.databinding.ActivityAddStoryBinding
import com.callcenter.storydicoding.view.custom.CustomEditText
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException

class AddStoryActivity : AppCompatActivity() {
    private lateinit var editTextStoryDescription: CustomEditText
    private lateinit var buttonAddStory: Button
    private lateinit var imageViewStory: ImageView
    private lateinit var buttonCamera: Button
    private lateinit var buttonGallery: Button
    private lateinit var progressBar: ProgressBar
    private var selectedImageUri: Uri? = null
    private lateinit var photoFile: File

    private lateinit var binding: ActivityAddStoryBinding

    private lateinit var viewModel: StoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Add Story"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white, null))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(resources.getColor(android.R.color.white, null))

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        editTextStoryDescription = binding.editTextStoryDescription
        buttonAddStory = binding.buttonAddStory
        imageViewStory = binding.imageViewStory
        buttonCamera = binding.buttonCamera
        buttonGallery = binding.buttonGallery
        progressBar = binding.progressBar

        // Get instance of UserPreference and ApiService
        val userPreference = UserPreference.getInstance(dataStore)
        val apiService = ApiClient.apiService  // Get ApiService instance
        viewModel = ViewModelProvider(this, StoryViewModelFactory(userPreference, apiService)).get(StoryViewModel::class.java)

        buttonGallery.setOnClickListener {
            openGallery()
        }

        buttonCamera.setOnClickListener {
            openCamera()
        }

        buttonAddStory.setOnClickListener {
            val description = editTextStoryDescription.getText()
            if (description.isNotBlank() && selectedImageUri != null) {
                compressAndUploadStory(description, selectedImageUri!!)
            } else {
                Toast.makeText(this, "Please fill in the description and select an image", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.addStoryResponse.observe(this) { response ->
            progressBar.visibility = View.GONE
            if (response != null) {
                Toast.makeText(this, "Story added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                showErrorDialog("Error", "Failed to add story. Please try again.")
            }
        }

        buttonAddStory.setOnClickListener {
            val description = editTextStoryDescription.getText()
            if (description.isNotBlank() && selectedImageUri != null) {
                compressAndUploadStory(description, selectedImageUri!!)
            } else {
                Toast.makeText(this, "Please fill in the description and select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getResult.launch(intent)
    }

    private fun openCamera() {
        val picturesDir = getExternalFilesDir(null)?.let { File(it, "Pictures") }
        if (picturesDir != null && !picturesDir.exists()) {
            picturesDir.mkdirs()
        }

        photoFile = File(picturesDir, "story_image_${System.currentTimeMillis()}.jpg")
        val photoURI: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        getResultCamera.launch(intent)
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                imageViewStory.setImageURI(selectedImageUri)
            }
        }

    private val getResultCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImageUri = Uri.fromFile(photoFile)
                imageViewStory.setImageURI(selectedImageUri)
            }
        }

    private fun compressAndUploadStory(description: String, imageUri: Uri) {
        if (!isInternetAvailable()) {
            showErrorDialog("No Internet Connection", "Please check your internet connection and try again.")
            return
        }

        val originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))

        val originalStream = ByteArrayOutputStream()
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, originalStream)
        val originalSize = originalStream.size()
        Log.d("AddStoryActivity", "Original image size: $originalSize bytes")

        val resizedBitmap = resizeBitmap(originalBitmap, 800, 800)

        var compressQuality = 100
        val stream = ByteArrayOutputStream()

        do {
            stream.reset()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, stream)
            compressQuality -= 5
            Log.d("AddStoryActivity", "Trying compress quality: $compressQuality, size: ${stream.size()} bytes")
        } while (stream.size() > 1_024_000 && compressQuality > 0)

        if (stream.size() > 3_000_000) {
            showErrorDialog("Image too large", "The selected image is too large even after compression. Please select a smaller image.")
            return
        }

        val compressedFile = File(cacheDir, "compressed_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(compressedFile).use { out ->
            out.write(stream.toByteArray())
        }

        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), compressedFile)
        val body = MultipartBody.Part.createFormData("photo", compressedFile.name, requestFile)

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launchWhenStarted {
            viewModel.getSession().collect { user ->
                val descriptionBody = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
                val token = if (user.isLogin) "Bearer ${user.token}" else ""

                // Call the addNewStory method in ViewModel
                viewModel.addNewStory(token, descriptionBody, body)
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        val finalWidth: Int
        val finalHeight: Int

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight * ratioBitmap).toInt()
            finalHeight = maxHeight
        } else {
            finalWidth = maxWidth
            finalHeight = (maxWidth / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun showErrorDialog(title: String, message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.CAMERA)
        }

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                permissions.add(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        } else {
            Toast.makeText(this, "Permissions already granted.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty()) {
                var allPermissionsGranted = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false
                        break
                    }
                }

                if (allPermissionsGranted) {
                    Toast.makeText(this, "All permissions granted.", Toast.LENGTH_SHORT).show()
                } else {

                    Toast.makeText(this, "Some permissions were denied. Please enable them in settings.", Toast.LENGTH_LONG).show()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                        Toast.makeText(this, "Please allow Manage External Storage in settings.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }
}
