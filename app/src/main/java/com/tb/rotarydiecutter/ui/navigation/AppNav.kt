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
    NavHost(navController = navController, startDestination = "search") {

        composable("search") {
            SearchScreen(viewModel = viewModel, navController = navController)
        }

        composable("list") {
            DbScreen(viewModel = viewModel, navController = navController)
        }

        // Generic Add screen (no prefill)
        composable("add") {
            AddScreen(viewModel = viewModel, navController = navController)
        }

        // Detail screen
        composable(
            route = "rotary_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: -1
            DetailScreen(id = id, viewModel = viewModel, navController = navController)
        }

        // Add screen with optional DieCut prefill from query
        composable(
            route = "rotary_add?dieCut={dieCut}",
            arguments = listOf(
                navArgument("dieCut") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val dieCutPrefill = backStackEntry.arguments?.getString("dieCut").orEmpty()
            AddScreen(
                viewModel = viewModel,
                navController = navController,
                prefillDieCut = dieCutPrefill
            )
        }
    }
}
