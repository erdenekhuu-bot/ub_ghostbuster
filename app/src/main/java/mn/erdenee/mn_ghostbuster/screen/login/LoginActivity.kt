package mn.erdenee.mn_ghostbuster.screen.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mn.erdenee.mn_ghostbuster.R
import mn.erdenee.mn_ghostbuster.screen.home.HomeActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background){
                LoginScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreen(){
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Image(
            painter = painterResource(R.drawable.pic),
            contentDescription = "My image",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .aspectRatio(16f / 9f),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Join Us", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(32.dp))
        TextField(
            value = username,
            label = { Text("Username") },
            onValueChange = { username = it },
            modifier = Modifier.padding(horizontal = 16.dp),
            leadingIcon = { Icon(Icons.Rounded.AccountCircle, contentDescription = "") },
            shape = RoundedCornerShape(8.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            value = password,
            label = { Text("password") },
            onValueChange = { password = it },
            modifier = Modifier.padding(horizontal = 16.dp),
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = "") },
            shape = RoundedCornerShape(8.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                context.startActivity(Intent(context, HomeActivity::class.java))
            },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text("Login in")
        }
    }

}
