package mn.erdenee.mn_ghostbuster.screen.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mn.erdenee.mn_ghostbuster.screen.CaseScreen
import mn.erdenee.mn_ghostbuster.screen.MapScreen
import mn.erdenee.mn_ghostbuster.screen.MemberScreen
import mn.erdenee.mn_ghostbuster.screen.ProfileScreen

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = androidx.compose.material3.darkColorScheme(
                    background = Color(0xFF0F0F0F),
                    surface = Color(0xFF1A1A1A)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F0F0F)
                ) {
                    HomeScreen()
                }
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
        containerColor = Color(0xFF0F0F0F),
       bottomBar = { GhostBottomNavigation(navController) },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "Case") {
                composable("Case") { CaseScreen() }
                composable("Map") { MapScreen() }
                composable("Member") { MemberScreen() }
                composable("Profile") { ProfileScreen() }
            }
        }
    }
}

@Composable
fun GhostBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        color = Color(0xFF0F0F0F),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GhostNavItem(
                icon = Icons.Default.Album,
                label = "Газарууд",
                isSelected = currentRoute == "Case",
                onClick = {
                    navController.navigate("Case") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )

            GhostNavItem(
                icon = Icons.Default.LocationOn,
                label = "Байршил",
                isSelected = currentRoute == "Map",
                onClick = {
                    navController.navigate("Map") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )

            GhostNavItem(
                icon = Icons.Default.AccountBox,
                label = "Гишүүд",
                isSelected = currentRoute == "Member",
                onClick = {
                    navController.navigate("Member") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )

            GhostNavItem(
                icon = Icons.Default.Person,
                label = "Профайл",
                isSelected = currentRoute == "Profile",
                onClick = {
                    navController.navigate("Profile") {
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
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.height(56.dp)) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF99FF00) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            androidx.compose.material3.Text(
                text = label,
                color = if (isSelected) Color(0xFF99FF00) else Color.Gray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
