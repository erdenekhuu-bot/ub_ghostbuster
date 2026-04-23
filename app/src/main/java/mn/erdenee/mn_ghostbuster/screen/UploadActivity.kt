package mn.erdenee.mn_ghostbuster.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Album
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

            Text(
                "Тохиолдол бүртгэх",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                "Ямар нэг сонин хачин харагдсан зүйлүүдээ зураг, бичлэг болгоод оруулаарай",
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            ModernInputField(
                label = "Сонин хачин уу, сүнстэй газар уу",
                value = title,
                onValueChange = { title = it },
                placeholder = "Сүнстэй газар"
            )

            Spacer(modifier = Modifier.height(20.dp))

            ModernInputField(
                label = "Тайлбар оруулаарай",
                value = description,
                onValueChange = { description = it },
                placeholder = "Товч тайлбар",
                minHeight = 50.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

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

            UploadItem(
                label = "Зураг оруулах",
                icon = Icons.Default.PhotoCamera,
                hasFile = selectedImageUri != null,
                onClick = { imagePicker.launch("image/*") }
            )

            UploadItem(
                label = "Бичлэг оруулах",
                icon = Icons.Default.Album,
                hasFile = selectedVideoUri != null,
                onClick = { videoPicker.launch("video/*") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Буцах",
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
                        Text("Хадгалах", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
        Text("Байршилын координат", color = Color(0xFF99FF00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onGetCoordinates,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3B23)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF99FF00))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Байршил доторхойлох", color = Color(0xFF99FF00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        val coords = address.split(",")
        Text("Өргөрөг: ${coords.getOrNull(0)?.trim() ?: "---"}", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text("Уртраг: ${coords.getOrNull(1)?.trim() ?: "---"}", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
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
