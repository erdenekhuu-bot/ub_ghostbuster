package mn.erdenee.mn_ghostbuster.screen.signup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mn.erdenee.mn_ghostbuster.R
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mn.erdenee.mn_ghostbuster.api.APIClient
import mn.erdenee.mn_ghostbuster.api.RetrofitCLient
import mn.erdenee.mn_ghostbuster.screen.login.LoginActivity
import mn.erdenee.mn_ghostbuster.types.RegisterRequest

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignUpScreen()
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun SignUpScreen(){
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.backgroundlayer),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.RadioButtonChecked,
                        contentDescription = null,
                        tint = Color(0xFFD8B4FE),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Сүнсний ангуучид",
                        color = Color(0xFFD8B4FE),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp
                    )
                }

            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color(0xE6111111))
                    .padding(horizontal = 24.dp, vertical = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Паранормал апп",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "Монголын паранормал үзэгдэл сонирхогчдод зориулав",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                AuthInputFieldLabel("БҮРТГҮҮЛЭХ НЭР")
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Өөрийн хүссэнээр", color = Color(0xFF374151)) },
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                AuthInputFieldLabel("НУУЦ ҮГ")
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("........", color = Color(0xFF374151)) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                AuthInputFieldLabel("УТАСНЫ ДУГААР")
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {Text("Утасны дугаар",color = Color(0xFF374151))},
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFA78BFA))
                } else {
                    val authGradient = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFC084FC), Color(0xFF818CF8))
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                val apiService = RetrofitCLient().getInstance().create(APIClient::class.java)
                                val request = RegisterRequest(username, password,phone)
                                val response = apiService.register(request)
                                if(response.isSuccessful) context.startActivity(Intent(context, LoginActivity::class.java))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(authGradient),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Бүртгүүлэх",
                            color = Color(0xFF1E1B4B),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(modifier = Modifier.width(40.dp).height(1.dp).background(Color(0xFF262626)))

                Row {
                    Text("Бүртгэлтэй гишүүн үү? ", color = Color(0xFF6B7280), fontSize = 12.sp)
                    Text("Нэвтрээрэй",  color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable{
                        context.startActivity(Intent(context, LoginActivity::class.java))
                    })
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("40.7128°", color = Color(0xFF4D7C0F), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("74.0060°", color = Color(0xFF4D7C0F), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                Text("БОЛГООМЖТОЙ БАЙГААРАЙ", color = Color(0xFF4B5563), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
@Composable
private fun AuthInputFieldLabel(text: String) {
    Text(
        text,
        color = Color(0xFF6B7280),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF070707),
    unfocusedContainerColor = Color(0xFF070707),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF166534),
    unfocusedBorderColor = Color(0xFF1F2937),
    cursorColor = Color(0xFF22C55E)
)
