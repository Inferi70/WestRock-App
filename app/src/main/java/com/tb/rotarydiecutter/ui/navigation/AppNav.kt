package com.tb.rotarydiecutter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.tb.rotarydiecutter.models.RotaryView
import com.tb.rotarydiecutter.ui.screens.AddScreen
import com.tb.rotarydiecutter.ui.screens.DbScreen
import com.tb.rotarydiecutter.ui.screens.DetailScreen
import com.tb.rotarydiecutter.ui.screens.SearchScreen

@Composable
fun AppNav(navController: NavHostController, viewModel: RotaryView) {
    NavHost(navController, startDestination = "search") {
        composable("search") { SearchScreen(viewModel, navController) }
        composable("list") { DbScreen(viewModel, navController) }
        composable("add") { AddScreen(viewModel) }
        composable(
            "rotary_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) {
            val id = it.arguments?.getInt("id") ?: -1
            DetailScreen(id, viewModel, navController)
        }
    }
}
