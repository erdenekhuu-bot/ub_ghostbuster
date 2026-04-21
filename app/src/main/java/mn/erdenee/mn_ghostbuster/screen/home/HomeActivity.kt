package mn.erdenee.mn_ghostbuster.screen.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mn.erdenee.mn_ghostbuster.screen.CaseScreen
import mn.erdenee.mn_ghostbuster.screen.MapScreen
import mn.erdenee.mn_ghostbuster.screen.MemberScreen

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HomeScreen(){
    val navController = rememberNavController()

    Scaffold(
       bottomBar = { GhostBottomNavigation(navController) },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "Case") {
                composable("Case") { CaseScreen() }
                composable("Map") { MapScreen() }
                composable("Member") { MemberScreen() }
            }
        }
    }
}

@Composable
fun GhostBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    Surface(
        color = Color(0xFF0F0F0F),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GhostNavItem(
                painter = painterResource(android.R.drawable.ic_menu_sort_by_size),
                isSelected = currentRoute == "Case",
                onClick = {
                    navController.navigate("Case") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )

            GhostNavItem(
                painter = painterResource(android.R.drawable.ic_dialog_map),
                isSelected = currentRoute == "Map",
                onClick = {
                    navController.navigate("Map") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
//            GhostNavItem(
//                painter = painterResource(android.R.drawable.ic_menu_add),
//                isSelected = false,
//                onClick = {
//                    context.startActivity(Intent(context, UploadActivity::class.java))
//                }
//            )

            GhostNavItem(
                painter = painterResource(android.R.drawable.ic_menu_myplaces),
                isSelected = currentRoute == "Member",
                onClick = {
                    navController.navigate("Member") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun GhostNavItem(
    painter: Painter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF99FF00) else Color.Gray
        )
    }
}
