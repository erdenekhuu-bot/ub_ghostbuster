//package mn.erdenee.mn_ghostbuster.screen
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Bundle
//import android.provider.OpenableColumns
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.aspectRatio
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.MyLocation
//import androidx.compose.material3.Button
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import coil.compose.rememberAsyncImagePainter
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.Priority
//import com.google.android.gms.tasks.CancellationTokenSource
//import kotlinx.coroutines.launch
//import mn.erdenee.mn_ghostbuster.api.APIClient
//import mn.erdenee.mn_ghostbuster.api.RetrofitCLient
//import mn.erdenee.mn_ghostbuster.api.SessionManager
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.RequestBody.Companion.asRequestBody
//import okhttp3.RequestBody.Companion.toRequestBody
//import java.io.File
//import java.io.FileOutputStream
//import java.util.Objects
//
//class UploadActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            UploadScreen()
//        }
//    }
//}
//// ai added here
//
//@Preview(showBackground = true)
//@Composable
//fun UploadScreen() {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val sessionManager = remember { SessionManager(context) }
//    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
//
//    var title by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var address by remember { mutableStateOf("") }
//    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
//    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
//    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
//    var isLoading by remember { mutableStateOf(false) }
//
//    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
//        if (success) {
//            selectedImageUri = tempImageUri
//        }
//    }
//
//    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//        if (isGranted) {
//            val uri = FileProvider.getUriForFile(
//                Objects.requireNonNull(context),
//                context.packageName + ".fileprovider",
//                createTempFile(context)
//            )
//            tempImageUri = uri
//            cameraLauncher.launch(uri)
//        } else {
//            Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    val locationPermissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
//            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
//        ) {
//            getCurrentLocation(context, fusedLocationClient) { lat, lon ->
//                address = "$lat, $lon"
//            }
//        } else {
//            Toast.makeText(context, "Location Permission Denied", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        selectedImageUri = uri
//    }
//
//    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        selectedVideoUri = uri
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(rememberScrollState()),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Шинэ байршил нэмэх", style = MaterialTheme.typography.headlineMedium)
//        Spacer(modifier = Modifier.height(16.dp))
//        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
//        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
//
//        OutlinedTextField(
//            value = address,
//            onValueChange = { address = it },
//            label = { Text("Address (Lat, Lon)") },
//            modifier = Modifier.fillMaxWidth(),
//            trailingIcon = {
//                Icon(
//                    imageVector = Icons.Default.MyLocation,
//                    contentDescription = "Get Current Location",
//                    modifier = Modifier.clickable {
//                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
//                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                            getCurrentLocation(context, fusedLocationClient) { lat, lon ->
//                                address = "$lat, $lon"
//                            }
//                        } else {
//                            locationPermissionLauncher.launch(
//                                arrayOf(
//                                    Manifest.permission.ACCESS_FINE_LOCATION,
//                                    Manifest.permission.ACCESS_COARSE_LOCATION
//                                )
//                            )
//                        }
//                    }
//                )
//            }
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        if (selectedImageUri != null) {
//            Image(
//                painter = rememberAsyncImagePainter(selectedImageUri),
//                contentDescription = null,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .aspectRatio(16f / 9f)
//                    .padding(8.dp),
//                contentScale = ContentScale.Crop
//            )
//        }
//
//        Button(onClick = { imagePicker.launch("image/*") }) {
//            Text("Select Image from Gallery")
//        }
//
//        Button(onClick = { videoPicker.launch("video/*") }) {
//            Text(if (selectedVideoUri != null) "Video Selected" else "Select Video")
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        if (isLoading) {
//            CircularProgressIndicator()
//        } else {
//            Button(
//                onClick = {
//                    val token = sessionManager.getAccessToken()
//                    if (token == null) {
//                        Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
//                        return@Button
//                    }
//                    isLoading = true
//                    scope.launch {
//                        try {
//                            val apiService = RetrofitCLient().getInstance().create(APIClient::class.java)
//
//                            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
//                            val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
//                            val addressPart = address.toRequestBody("text/plain".toMediaTypeOrNull())
//
//                            val imageParts = mutableListOf<MultipartBody.Part>()
//                            selectedImageUri?.let { uri ->
//                                val file = getFileFromUri(context, uri, "image.jpg")
//                                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
//                                imageParts.add(MultipartBody.Part.createFormData("image", file.name, requestFile))
//                            }
//
//                            val videoParts = mutableListOf<MultipartBody.Part>()
//                            selectedVideoUri?.let { uri ->
//                                val file = getFileFromUri(context, uri, "video.mp4")
//                                val requestFile = file.asRequestBody("video/*".toMediaTypeOrNull())
//                                videoParts.add(MultipartBody.Part.createFormData("video", file.name, requestFile))
//                            }
//
//                            val response = apiService.uploadLocation(
//                                token = "Bearer $token",
//                                title = titlePart,
//                                description = descriptionPart,
//                                address = addressPart,
//                                image = imageParts,
//                                video = videoParts
//                            )
//
//                            if (response.isSuccessful) {
//                                Toast.makeText(context, "Upload Success!", Toast.LENGTH_SHORT).show()
//                                (context as? AppCompatActivity)?.finish()
//                            } else {
//                                Toast.makeText(context, "Upload Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
//                            }
//                        } catch (e: Exception) {
//                            Log.e("Upload", "Error", e)
//                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                        } finally {
//                            isLoading = false
//                        }
//                    }
//                },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Upload Location")
//            }
//        }
//    }
//}
//
//@SuppressLint("MissingPermission")
//private fun getCurrentLocation(
//    context: Context,
//    fusedLocationClient: FusedLocationProviderClient,
//    onLocationReceived: (Double, Double) -> Unit
//) {
//    val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//    val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//
//    if (!hasFineLocation && !hasCoarseLocation) return
//
//    val priority = if (hasFineLocation) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
//
//    fusedLocationClient.getCurrentLocation(priority, CancellationTokenSource().token)
//        .addOnSuccessListener { location ->
//            if (location != null) {
//                onLocationReceived(location.latitude, location.longitude)
//            } else {
//                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
//                    if (lastLoc != null) {
//                        onLocationReceived(lastLoc.latitude, lastLoc.longitude)
//                    } else {
//                        Toast.makeText(context, "Location not found. Please enable GPS.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//        .addOnFailureListener { e ->
//            Toast.makeText(context, "Location Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//        }
//}
//
//private fun createTempFile(context: Context): File {
//    val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
//    tempFile.createNewFile()
//    return tempFile
//}
//
//private fun getFileFromUri(context: Context, uri: Uri, defaultName: String): File {
//    val contentResolver = context.contentResolver
//
//    val fileName = try {
//        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
//            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//            if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
//        }
//    } catch (e: Exception) {
//        null
//    } ?: defaultName
//
//    val file = File(context.cacheDir, fileName)
//    try {
//        contentResolver.openInputStream(uri)?.use { input ->
//            FileOutputStream(file).use { output ->
//                input.copyTo(output)
//            }
//        }
//    } catch (e: Exception) {
//        Log.e("Upload", "Error copying file", e)
//    }
//    return file
//}

package mn.erdenee.mn_ghostbuster.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
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

class UploadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF0A0A0A),
                    surface = Color(0xFF121212)
                )
            ) {
                UploadScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UploadScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            getCurrentLocation(context, fusedLocationClient) { lat, lon ->
                address = "$lat, $lon"
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedVideoUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF99FF00), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "NEW SESSION INITIALIZE",
                    color = Color(0xFF99FF00),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Report Activity",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                "Archive a new spectral occurrence. Ensure all technical metadata is captured for accurate post-investigation analysis.",
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title Field
            ModernInputField(
                label = "ARCHIVE IDENTIFIER (TITLE)",
                value = title,
                onValueChange = { title = it },
                placeholder = "e.g., PHANTOM_RESONANCE"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Description Field
            ModernInputField(
                label = "SPECTRAL NARRATIVE (DESCRIPTION)",
                value = description,
                onValueChange = { description = it },
                placeholder = "Detail the visual artifacts, atmospheric shifts, and chronological progression of the event...",
                singleLine = false,
                minHeight = 120.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Location Section
            LocationCard(
                address = address,
                onGetCoordinates = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getCurrentLocation(context, fusedLocationClient) { lat, lon ->
                            address = "$lat, $lon"
                        }
                    } else {
                        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Upload Sections
            UploadItem(
                label = "UPLOAD IMAGE",
                icon = Icons.Default.PhotoCamera,
                hasFile = selectedImageUri != null,
                onClick = { imagePicker.launch("image/*") }
            )

            UploadItem(
                label = "SELECT EVIDENCE VIDEO",
                icon = Icons.Default.Mic, // Using Mic as placeholder for audio/video style
                hasFile = selectedVideoUri != null,
                onClick = { videoPicker.launch("video/*") }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Warning
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Report will be uploaded to the global spectral database. Ensure accuracy.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "DISCARD",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { (context as? AppCompatActivity)?.finish() }
                )

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFA78BFA))
                } else {
                    val gradient = Brush.horizontalGradient(listOf(Color(0xFFC084FC), Color(0xFF818CF8)))
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
                                        Toast.makeText(context, "Commit Successful!", Toast.LENGTH_SHORT).show()
                                        (context as? AppCompatActivity)?.finish()
                                    } else {
                                        Toast.makeText(context, "Commit Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(gradient),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text("COMMIT EVIDENCE", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 0.dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.heightIn(min = minHeight)) {
            if (value.isEmpty()) {
                Text(placeholder, color = Color(0xFF333333), fontSize = 16.sp)
            }
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine
            )
        }
    }
}

@Composable
fun LocationCard(address: String, onGetCoordinates: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("SPATIAL COORDINATES", color = Color(0xFF99FF00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Mock Map Preview Style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F0F0F))
        ) {
            // Placeholder map image or actual map could go here
            Icon(
                Icons.Default.MyLocation,
                contentDescription = null,
                tint = Color(0xFF99FF00),
                modifier = Modifier.align(Alignment.Center).size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGetCoordinates,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3B23)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF99FF00))
                Spacer(modifier = Modifier.width(8.dp))
                Text("GET MY COORDINATES", color = Color(0xFF99FF00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        val coords = address.split(",")
        Text("LAT: ${coords.getOrNull(0)?.trim() ?: "---"}", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text("LONG: ${coords.getOrNull(1)?.trim() ?: "---"}", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun UploadItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, hasFile: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF121212), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFFD8B4FE), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Icon(
            if (hasFile) Icons.Rounded.RadioButtonChecked else Icons.Default.Add,
            contentDescription = null,
            tint = if (hasFile) Color(0xFF99FF00) else Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}

@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit
) {
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
        .addOnSuccessListener { location ->
            if (location != null) onLocationReceived(location.latitude, location.longitude)
        }
}

private fun getFileFromUri(context: Context, uri: Uri, defaultName: String): File {
    val contentResolver = context.contentResolver
    val fileName = defaultName
    val file = File(context.cacheDir, fileName)
    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file
}
