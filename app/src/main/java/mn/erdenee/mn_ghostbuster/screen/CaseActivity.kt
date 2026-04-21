package mn.erdenee.mn_ghostbuster.screen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mn.erdenee.mn_ghostbuster.api.APIClient
import mn.erdenee.mn_ghostbuster.types.LocationResult
import mn.erdenee.mn_ghostbuster.api.RetrofitCLient
import mn.erdenee.mn_ghostbuster.api.SessionManager
import mn.erdenee.mn_ghostbuster.ui.BoxUI

class CaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CaseScreen()
        }
    }
}
@Preview(showBackground = true)
@Composable
fun CaseScreen() {
    val apiService = remember { RetrofitCLient().getInstance().create(APIClient::class.java) }

    // State variables
    val locations = remember { mutableStateListOf<LocationResult>() }
    var page by remember { mutableIntStateOf(1) }
    var canLoadMore by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.getAccessToken() ?: ""

    // Pagination Logic
    fun loadNextPage() {
        if (isLoading || !canLoadMore) return

        isLoading = true
        scope.launch {
            try {
                // Call the location list API
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

    // Initial trigger
    LaunchedEffect(Unit) {
        loadNextPage()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(locations) { item ->
            BoxUI(
                title = item.title,         // "gmobile", "Жамьян Гүний..."
                content = item.address ?: "" // "47.9244537, 106.919..."
            )
        }

        // Loading and Infinite Scroll Trigger
        item {
            LaunchedEffect(locations.size) {
                if (canLoadMore && !isLoading && locations.isNotEmpty()) {
                    loadNextPage()
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}