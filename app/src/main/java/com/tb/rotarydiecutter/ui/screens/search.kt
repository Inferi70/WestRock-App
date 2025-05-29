package com.tb.rotarydiecutter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tb.rotarydiecutter.R
import com.tb.rotarydiecutter.models.RotaryView

@Composable
fun LabelValue(label: String, value: String?) {
    Column(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(
            text = "$label:",
            color = Color.Red,
            fontSize = 18.sp
        )
        Text(
            text = value ?: "",
            color = Color.White,
            fontSize = 20.sp
        )
    }
}

@Composable
fun SearchScreen(viewModel: RotaryView, navController: NavController) {
    var query by remember { mutableStateOf("") }
    val results by remember(query) { mutableStateOf(if (query != "") viewModel.search(query) else emptyList()) }

    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.stripesverticle),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search", fontSize = 26.sp, color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 24.sp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.Black,
                        focusedIndicatorColor = Color.Black,
                        unfocusedIndicatorColor = Color.Black,
                        cursorColor = Color.Red
                    )
                )
            }

            items(results) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("rotary_detail/${item.id}") }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        LabelValue("Die Cut", item.DieCut)
                        LabelValue("Box Per Hour", item.BPH)
                        LabelValue("Belt Speed", item.BeltSpeed)
                        LabelValue("Time Delay", item.TimeDelay)
                        LabelValue("Notes", item.Notes?.replace("\\n", "\n"))
                        LabelValue("Pull Roll", item.PullRoll?.toString())
                        LabelValue("Feed Gate", item.FeedGate?.toString())
                        LabelValue("Wheels Position", item.Wheels)
                        LabelValue("Running Out", item.BundleBreaker)
                        LabelValue("Scissor Lift", item.ScissorLift)
                        LabelValue("Skip Feed", item.Specialty)
                        LabelValue("Additional Notes:", item.Customer)
                    }
                }
            }
        }
    }
}
