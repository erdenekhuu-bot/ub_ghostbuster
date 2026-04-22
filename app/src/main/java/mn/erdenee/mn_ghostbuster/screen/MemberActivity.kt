package mn.erdenee.mn_ghostbuster.screen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import mn.erdenee.mn_ghostbuster.types.UserResult
import mn.erdenee.mn_ghostbuster.ui.BoxUI
import mn.erdenee.mn_ghostbuster.api.SessionManager
import mn.erdenee.mn_ghostbuster.types.LocationResult
import mn.erdenee.mn_ghostbuster.types.MemberResponse


class MemberActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemberScreen()
        }
    }
}
@Preview(showBackground = true)
@Composable
fun MemberScreen() {
    val apiService = remember { RetrofitCLient().getInstance().create(APIClient::class.java) }
    val members = remember { mutableStateListOf<UserResult>() }
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
                val response = apiService.members("Bearer $token", page, 5)
                if (response.isSuccessful) {
                    val newMembers = response.body()?.results ?: emptyList()
                    if (newMembers.isEmpty()) {
                        canLoadMore = false
                    } else {
                        members.addAll(newMembers)
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
    Scaffold(
        containerColor = Color(0xFF0F0F0F))
    { padding->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                HeaderSection()
            }

            items(members) { item ->
                InvestigationCard(item)
            }
//            items(members) { member ->
//                BoxUI(
//                    title = member.username,
//                    content = member.member?.status ?: ""
//                )
//            }

            item {
                LaunchedEffect(members.size) {
                    if (canLoadMore && !isLoading) {
                        loadNextPage()
                    }
                }

                if (isLoading) {
                    Box(Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun InvestigationCard(item: UserResult) { // Changed from MemberResponse to UserResult
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
                        item.username, // Now correctly accessible from UserResult
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
                        item.member?.status ?: "UNKNOWN", // Accessed via member property
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
