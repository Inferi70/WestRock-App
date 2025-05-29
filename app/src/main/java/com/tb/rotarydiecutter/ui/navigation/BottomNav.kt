package com.tb.rotarydiecutter.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        NavItem("Dies", "list", Icons.Default.List),
        NavItem("Search", "search", Icons.Default.Search),
        NavItem("Add", "add", Icons.Default.Add)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.navigate(item.route) {
                            popUpTo(0) // clear everything before navigating
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

data class NavItem(val label: String, val route: String, val icon: ImageVector)
