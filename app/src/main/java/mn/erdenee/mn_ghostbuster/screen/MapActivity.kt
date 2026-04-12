package mn.erdenee.mn_ghostbuster.screen


import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import mn.erdenee.mn_ghostbuster.api.APIClient
import mn.erdenee.mn_ghostbuster.api.LocationResult
import mn.erdenee.mn_ghostbuster.api.RetrofitCLient
import mn.erdenee.mn_ghostbuster.api.SessionManager
import androidx.compose.material.icons.outlined.Tornado
import androidx.compose.ui.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapScreen()
        }
    }
}
@Composable
fun MapScreen() {
    val apiService = remember { RetrofitCLient().getInstance().create(APIClient::class.java) }
    val locations = remember { mutableStateListOf<LocationResult>() }

    var page by remember { mutableIntStateOf(1) }
    var canLoadMore by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.getAccessToken() ?: ""

    // Center of Ulaanbaatar
    val ulaanbaatar = LatLng(47.921230, 106.918556)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ulaanbaatar, 13f)
    }

    fun loadNextPage() {
        if (isLoading || !canLoadMore) return

        isLoading = true
        scope.launch {
            try {
                val response = apiService.locations("Bearer $token", page, 10)
                if (response.isSuccessful) {
                    val newLocations = response.body()?.results ?: emptyList()
                    if (newLocations.isEmpty()) {
                        canLoadMore = false
                    } else {
                        locations.addAll(newLocations)
                        page++
                    }
                }
                Log.d("here", locations.toString())
            } finally {
                isLoading = false
            }
        }
    }
    LaunchedEffect(Unit) {
        loadNextPage()
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        locations.forEach { location ->
            key(location.id) {
                val coords = location.address.split(",")
                if (coords.size == 2) {
                    val lat = coords[0].trim().toDoubleOrNull()
                    val lng = coords[1].trim().toDoubleOrNull()

                    if (lat != null && lng != null) {
                        Marker(
                            state = rememberMarkerState(position = LatLng(lat, lng)),
                            title = location.title,
                            snippet = "ID: ${location.id}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                        )
                    }
                }
            }
        }
    }
}


