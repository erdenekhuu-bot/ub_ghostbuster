package mn.erdenee.mn_ghostbuster.screen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import mn.erdenee.mn_ghostbuster.api.APIClient
import mn.erdenee.mn_ghostbuster.api.RetrofitCLient
import mn.erdenee.mn_ghostbuster.api.SessionManager
import mn.erdenee.mn_ghostbuster.types.LocationResult

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
    var selectedLocation by remember { mutableStateOf<LocationResult?>(null) }

    var page by remember { mutableIntStateOf(1) }
    var canLoadMore by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.getAccessToken() ?: ""

    // Dark Map Style JSON
    val darkMapStyle = """
        [
          { "elementType": "geometry", "stylers": [ { "color": "#212121" } ] },
          { "elementType": "labels.icon", "stylers": [ { "visibility": "off" } ] },
          { "elementType": "labels.text.fill", "stylers": [ { "color": "#757575" } ] },
          { "elementType": "labels.text.stroke", "stylers": [ { "color": "#212121" } ] },
          { "featureType": "administrative", "elementType": "geometry", "stylers": [ { "color": "#757575" } ] },
          { "featureType": "poi", "elementType": "geometry", "stylers": [ { "color": "#181818" } ] },
          { "featureType": "road", "elementType": "geometry.fill", "stylers": [ { "color": "#2c2c2c" } ] },
          { "featureType": "road", "elementType": "labels.text.fill", "stylers": [ { "color": "#8a8a8a" } ] },
          { "featureType": "water", "elementType": "geometry", "stylers": [ { "color": "#000000" } ] }
        ]
    """.trimIndent()

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
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadNextPage()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Google Map Background
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapStyleOptions = MapStyleOptions(darkMapStyle)),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            locations.forEach { location ->
                val coords = location.address.split(",")
                if (coords.size == 2) {
                    val lat = coords[0].trim().toDoubleOrNull()
                    val lng = coords[1].trim().toDoubleOrNull()
                    if (lat != null && lng != null) {
                        Marker(
                            state = rememberMarkerState(position = LatLng(lat, lng)),
                            title = location.title,
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (location.id % 2 == 0) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_VIOLET
                            ),
                            onClick = {
                                selectedLocation = location
                                false
                            }
                        )
                    }
                }
            }
        }

        // 2. Top Bar Overlay
        TopMapOverlay()

        // 3. Bottom Detail Panel & FAB
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            Column(horizontalAlignment = Alignment.End) {
                // Neon FAB
                FloatingActionButton(
                    onClick = { /* Add Action */ },
                    containerColor = Color(0xFF99FF00),
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp).size(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }

                // Info Sheet (Persistent Overlay Style)
                LocationDetailSheet(selectedLocation)
            }
        }
    }
}

@Composable
fun TopMapOverlay() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.RadioButtonChecked,
                contentDescription = null,
                tint = Color(0xFFD8B4FE),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "UB_GHOSTBUSTER",
                color = Color(0xFFD8B4FE),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 2.sp
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            // Profile Image Placeholder
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            ) {
                Image(
                    painter = painterResource(android.R.drawable.ic_menu_gallery),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun LocationDetailSheet(location: LocationResult?) {
    // If no location is selected, show a default or empty sheet matching the design
    val displayTitle = location?.title ?: "Select a point"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header: Status and EMF
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF991B1B),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "HIGH ALERT",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ZONE 7-A", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        displayTitle,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("2.4km • Last ping 14m ago", color = Color.Gray, fontSize = 12.sp)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("98.4 μT", color = Color(0xFF99FF00), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("EMF INTENSITY", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("THERMAL DROP", "-12.4°C", Modifier.weight(1f))
                MetricCard("EVP PROBABILITY", "84%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
