package mn.erdenee.mn_ghostbuster.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import mn.erdenee.mn_ghostbuster.api.APIClient
import mn.erdenee.mn_ghostbuster.types.LocationResult
import mn.erdenee.mn_ghostbuster.api.RetrofitCLient
import mn.erdenee.mn_ghostbuster.api.SessionManager

class CaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF0F0F0F),
                    surface = Color(0xFF1A1A1A)
                )
            ) {
                CaseScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaseScreen() {
    val apiService = remember { RetrofitCLient().getInstance().create(APIClient::class.java) }

    val locations = remember { mutableStateListOf<LocationResult>() }
    var page by remember { mutableIntStateOf(1) }
    var canLoadMore by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.getAccessToken() ?: ""

    fun loadNextPage() {
        if (isLoading || !canLoadMore) return
        isLoading = true
        scope.launch {
            try {
                val response = apiService.locations("Bearer $token", page, 5)
                if (response.isSuccessful) {
                    val newLocations = response.body()?.results ?: emptyList()
                    if (newLocations.isEmpty()) {
                        canLoadMore = false
                    } else {
                        locations.addAll(newLocations)
                        page++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadNextPage()
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, UploadActivity::class.java))
                },
                containerColor = Color(0xFF99FF00),
                contentColor = Color.Black,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                HeaderSection()
            }

            items(locations) { item ->
                InvestigationCard(item)
            }

            item {
                LaunchedEffect(locations.size) {
                    if (canLoadMore && !isLoading && locations.isNotEmpty()) {
                        loadNextPage()
                    }
                }
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFD8B4FE))
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Газарууд хайх",
                color = Color(0xFFD8B4FE),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.5.sp
            )
        }
        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
    }
}

@Composable
fun HeaderSection() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            "Active\nInvestigations",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "ARCHIVE FREQUENCY: 432HZ",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun InvestigationCard(item: LocationResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1509248961158-e54f6934749c",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                // Badges
                Row(modifier = Modifier.padding(12.dp)) {
                    Badge("EXTREME", Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    LiveScanBadge()
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SPECTRAL", color = Color.Gray, fontSize = 8.sp)
                        Text("SIGNATURE", color = Color.Gray, fontSize = 8.sp)
                        Text("9.2mHz", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        item.address ?: "UNKNOWN LOCATION",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun Badge(text: String, color: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(50),
        modifier = Modifier.height(20.dp)
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun LiveScanBadge() {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .height(20.dp)
            .border(1.dp, Color(0xFF99FF00), RoundedCornerShape(50))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Box(modifier = Modifier.size(4.dp).background(Color(0xFF99FF00), CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "LIVE SCAN",
                color = Color(0xFF99FF00),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}