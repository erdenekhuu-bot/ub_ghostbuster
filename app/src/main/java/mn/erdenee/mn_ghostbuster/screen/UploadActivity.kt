package mn.erdenee.mn_ghostbuster.screen

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mn.erdenee.mn_ghostbuster.api.APIClient
import mn.erdenee.mn_ghostbuster.api.RetrofitCLient
import mn.erdenee.mn_ghostbuster.api.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class UploadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UploadScreen()
        }
    }
}

@Composable
fun UploadScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedVideoUri = uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        if (!cameraGranted) {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create New Location", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA)) }) {
            Text("Request Camera Permission")
        }

        Button(onClick = { imagePicker.launch("image/*") }) {
            Text(if (selectedImageUri != null) "Image Selected" else "Select Image")
        }

        Button(onClick = { videoPicker.launch("video/*") }) {
            Text(if (selectedVideoUri != null) "Video Selected" else "Select Video")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val token = sessionManager.getAccessToken()
                    if (token == null) {
                        Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            val apiService = RetrofitCLient().getInstance().create(APIClient::class.java)

                            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
                            val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                            val addressPart = address.toRequestBody("text/plain".toMediaTypeOrNull())

                            val imageParts = mutableListOf<MultipartBody.Part>()
                            selectedImageUri?.let { uri ->
                                val file = getFileFromUri(context, uri, "image.jpg")
                                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                imageParts.add(MultipartBody.Part.createFormData("image", file.name, requestFile))
                            }

                            val videoParts = mutableListOf<MultipartBody.Part>()
                            selectedVideoUri?.let { uri ->
                                val file = getFileFromUri(context, uri, "video.mp4")
                                val requestFile = file.asRequestBody("video/*".toMediaTypeOrNull())
                                videoParts.add(MultipartBody.Part.createFormData("video", file.name, requestFile))
                            }

                            val response = apiService.uploadLocation(
                                token = "Bearer $token",
                                title = titlePart,
                                description = descriptionPart,
                                address = addressPart,
                                image = imageParts,
                                video = videoParts
                            )

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Upload Success!", Toast.LENGTH_SHORT).show()
                                (context as? AppCompatActivity)?.finish()
                            } else {
                                Toast.makeText(context, "Upload Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("Upload", "Error", e)
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload Location")
            }
        }
    }
}

private fun getFileFromUri(context: Context, uri: Uri, defaultName: String): File {
    val contentResolver = context.contentResolver
    val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst()) cursor.getString(nameIndex) else defaultName
    } ?: defaultName

    val file = File(context.cacheDir, fileName)
    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file
}
