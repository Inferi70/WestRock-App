package com.tb.rotarydiecutter.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable   // ← ensure this import
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(navController: NavController) {
    var bowlingMode by rememberSaveable { mutableStateOf(false) }

    val diesItems = listOf(
        NavItem("Dies", "list", Icons.Filled.List),
        NavItem("Search", "search", Icons.Filled.Search),
        NavItem("Add", "add", Icons.Filled.Add)
    )
    val bowlItems = listOf(
        NavItem("Home", "bowl/home", Icons.Filled.Sports),
        NavItem("Add", "bowl/add", Icons.Filled.Add),
        NavItem("History", "bowl/history", Icons.Filled.History)
    )

    val items = if (bowlingMode) bowlItems else diesItems

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Left-most toggle — label is ALWAYS "Bowling"
        NavigationBarItem(
            selected = bowlingMode,
            onClick = {
                bowlingMode = !bowlingMode
                // When switching modes, jump to the first tab in that mode
                val first = if (bowlingMode) "bowl/home" else "search"
                navController.navigate(first) {
                    popUpTo(0)
                    launchSingleTop = true
                }
            },
            icon = {
                Row {
                    Icon(Icons.Filled.Sports, contentDescription = "Bowling")
                    Spacer(Modifier.width(4.dp))
                }
            },
            label = { Text("Bowling") }   // ← fixed label
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

data class NavItem(val label: String, val route: String, val icon: ImageVector)