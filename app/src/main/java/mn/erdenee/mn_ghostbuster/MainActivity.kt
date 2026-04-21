package mn.erdenee.mn_ghostbuster

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import mn.erdenee.mn_ghostbuster.screen.login.LoginScreen
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
    @Override
    override fun onStart() {
        super.onStart()
    }
}