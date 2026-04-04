package mn.erdenee.mn_ghostbuster.screen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mn.erdenee.mn_ghostbuster.ui.BoxUI

class MemberActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemberScreen()
        }
    }
}
@Composable
fun MemberScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        val scrollState=rememberScrollState()
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
        ) {
            for (x in 1..20) {
                BoxUI(title = "Item $x", content = "Description for $x")
            }
        }
    }
}