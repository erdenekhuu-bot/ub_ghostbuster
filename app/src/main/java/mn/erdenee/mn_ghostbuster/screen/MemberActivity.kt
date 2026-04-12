package mn.erdenee.mn_ghostbuster.screen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
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
import mn.erdenee.mn_ghostbuster.api.RetrofitCLient
import mn.erdenee.mn_ghostbuster.api.UserResult
import mn.erdenee.mn_ghostbuster.ui.BoxUI
import mn.erdenee.mn_ghostbuster.api.SessionManager

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
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(members) { member ->
            BoxUI(
                title = member.username,
                content = member.member?.status ?: ""
            )
        }

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

