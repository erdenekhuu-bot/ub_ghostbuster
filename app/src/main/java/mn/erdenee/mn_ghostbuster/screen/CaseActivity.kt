package mn.erdenee.mn_ghostbuster.screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import mn.erdenee.mn_ghostbuster.api.RetrofitCLient
import mn.erdenee.mn_ghostbuster.api.SessionManager
import mn.erdenee.mn_ghostbuster.types.CaseBody

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

    val locations = remember { mutableStateListOf<CaseBody>() }
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
fun InvestigationCard(item: CaseBody) {
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
                    model = "${item.image.file}",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        item.address ?: "",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}