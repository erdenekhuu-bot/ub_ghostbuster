package mn.erdenee.mn_ghostbuster.screen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val accessToken = sessionManager.getAccessToken() ?: ""

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Displaying the token as an example
        Text(
            text = "Saved Access Token:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = accessToken,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        for (x in 1..20) {
            BoxUI(title = "Item $x", content = "Description for $x")
        }
    }
}
