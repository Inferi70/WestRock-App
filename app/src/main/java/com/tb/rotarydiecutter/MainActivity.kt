package com.tb.rotarydiecutter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tb.rotarydiecutter.models.RotaryView
import com.tb.rotarydiecutter.ui.navigation.AppNav
import com.tb.rotarydiecutter.ui.navigation.BottomNavBar
import com.tb.rotarydiecutter.ui.theme.FinalAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rotaryViewModel = RotaryView(applicationContext)

        setContent {
            FinalAppTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNav(
                            navController = navController,
                            viewModel = rotaryViewModel
                        )
                    }
                }
            }
        }
    }
}