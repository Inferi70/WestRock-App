package com.tb.rotarydiecutter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tb.rotarydiecutter.R
import com.tb.rotarydiecutter.models.RotaryView

@Composable
fun DetailScreen(id: Int, viewModel: RotaryView, navController: NavController) {

    //Update the last_accessed field any time we open screen info
    LaunchedEffect(id) {
        viewModel.updateLastAccessed(id)
        viewModel.incrementAccessedCount(id)
    }

    val item = viewModel.dummyItems.find { it.id == id }
    if (item == null) {
        Text("Item not found", modifier = Modifier.padding(16.dp))
        return
    }

    var isEditing by remember { mutableStateOf(false) }

    var f1 by remember { mutableStateOf(item.DieCut ?: "") }
    var f2 by remember { mutableStateOf(item.BeltSpeed ?: "") }
    var f3 by remember { mutableStateOf(item.TimeDelay ?: "") }
    var f4 by remember { mutableStateOf(item.BPH ?: "") }
    var f5 by remember { mutableStateOf(item.Customer ?: "") }
    var f6 by remember { mutableStateOf(item.Notes ?: "") }

    var wheels by remember { mutableStateOf(item.Wheels ?: "Up") }
    var bundleBreaker by remember { mutableStateOf(item.BundleBreaker ?: "No") }
    var scissorLift by remember { mutableStateOf(item.ScissorLift ?: "No") }
    var specialty by remember { mutableStateOf(item.Specialty ?: "No") }
    var pullRoll by remember { mutableStateOf(item.PullRoll?.toString() ?: "") }
    var feedGate by remember { mutableStateOf(item.FeedGate?.toString() ?: "") }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.blackwhite),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.90f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailField(
                label = "Die Cut",
                value = f1,
                isEditing = isEditing,
                labelFontWeight = FontWeight.Bold,
                labelFontSize = 36,
                imeAction = ImeAction.Done,
                whiteFontSize = 64,
                whiteFontColor = Color(0xE8F3E913)
            ) { f1 = it }
            DetailField("Box Per Hour", f4, isEditing, imeAction = ImeAction.Done) { f4 = it }

            if (isEditing) {
                Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFC5C5C5).copy(alpha = 0.85f), thickness = 3.dp)
                VerticalRadioField("Scissor Lift", scissorLift, listOf("Yes", "Unknown")) {
                    scissorLift = it
                }
                Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFC5C5C5).copy(alpha = 0.85f), thickness = 3.dp)
                VerticalRadioField("Skip Feed", specialty, listOf("Yes", "No", "Unknown")) {
                    specialty = it
                }
                Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFC5C5C5).copy(alpha = 0.85f), thickness = 3.dp)
            } else {
                DisplayLabelValue("Scissor Lift", scissorLift)
                DisplayLabelValue("Skip Feed", specialty)
            }

            DetailField("Belt Speed", f2, isEditing, imeAction = ImeAction.Done) { f2 = it }
            DetailField("Time Delay", f3, isEditing, imeAction = ImeAction.Done) { f3 = it }
            DetailField("Notes", f6, isEditing) { f6 = it }
            DetailField("Pull Roll", pullRoll, isEditing, imeAction = ImeAction.Done) {
                pullRoll = it
            }
            DetailField("Feed Gate", feedGate, isEditing, imeAction = ImeAction.Done) {
                feedGate = it
            }

            if (isEditing) {
                VerticalRadioField("Wheels", wheels, listOf("Up", "Down", "Unknown")) { wheels = it }
                Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFC5C5C5).copy(alpha = 0.85f), thickness = 3.dp)
            } else {
                DisplayLabelValue("Wheels", wheels)
            }

            DetailField("Additional Notes", f5, isEditing) { f5 = it }

            Button(
                onClick = {
                    if (isEditing) {
                        viewModel.updateItem(
                            id = id,
                            dieCut = f1.ifBlank { null },
                            beltSpeed = f2.ifBlank { null },
                            timeDelay = f3.ifBlank { null },
                            bph = f4.ifBlank { null },
                            customer = f5.ifBlank { null },
                            notes = f6.ifBlank { null },
                            wheels = wheels,
                            bundleBreaker = bundleBreaker,
                            scissorLift = scissorLift,
                            specialty = specialty,
                            pullRoll = pullRoll.toDoubleOrNull(),
                            feedGate = feedGate.toDoubleOrNull()
                        )
                    }
                    isEditing = !isEditing
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDA4717).copy(alpha = 0.85f),
                    contentColor = Color.White // optional: make text white
                )
            ) {
                Text(if (isEditing) "Save Changes" else "Edit", fontWeight = FontWeight.Bold, fontSize = 28.sp)
            }
            if (isEditing) {
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFA60000).copy(alpha = 0.85f), thickness = 5.dp)

                var showConfirmDialog by remember { mutableStateOf(false) }

                Button(
                    onClick = { viewModel.resetAccessedCount(item.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFA60000).copy(alpha = 0.85f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Reset Accessed Count", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFA60000).copy(alpha = 0.85f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    Text("Delete", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                if (showConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDialog = false },
                        title = { Text("Confirm Delete") },
                        text = { Text("Are you sure you want to delete this item?") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteById(item.id)
                                showConfirmDialog = false
                                navController.navigate("search") {
                                    popUpTo(0) // clears backstack
                                    launchSingleTop = true
                                }                        }) {
                                Text("Delete", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    isEditing: Boolean,
    labelFontWeight: FontWeight = FontWeight.Normal,
    labelFontSize: Int = 30,
    imeAction: ImeAction = ImeAction.Default,
    whiteFontSize: Int = 36,
    whiteFontColor: Color = Color.White,
    onChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, lineHeight = 36.sp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black,
                cursorColor = Color.Red
            ),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            )
        )

    } else {
        if (value.isNotBlank()) { // 👈 only show if not blank
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    label + ":",
                    color = Color.Red,
                    fontSize = labelFontSize.sp,
                    fontWeight = labelFontWeight
                )
                Text(value, color = whiteFontColor, fontSize = whiteFontSize.sp, lineHeight = 36.sp)
            }
        }
    }
}


@Composable
private fun RadioField(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(label + ":", color = Color.Red, fontSize = 30.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == option, onClick = { onSelect(option) })
                    Text(option, fontWeight = FontWeight.Bold, fontSize = 36.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun VerticalRadioField(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(label + ":", color = Color.Red, fontSize = 30.sp)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option) } // makes entire row clickable
                        .padding(vertical = 8.dp)       // increases touch area
                ) {
                    RadioButton(
                        selected = selected == option,
                        onClick = null, // handled by parent Row’s clickable
                        modifier = Modifier.size(32.dp) // ⬅ make button itself bigger
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplayLabelValue(label: String, value: String?) {
    if (value.isNullOrBlank()) return // 👈 don't show if empty/null

    // Special cases
    val lower = value.lowercase()
    if (label in listOf("Running Out", "Scissor Lift", "Skip Feed")) {
        if (lower == "yes" || lower == "up" || lower == "down") {
            // Just show the label (without ": Yes")
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(label + " Used", color = Color.Red, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
        // If "no", "unknown", or anything else → don't show at all
        return
    }

    if (label in listOf("Wheels")) {
        if (lower == "up" || lower == "down") {
            // Just show the label (without ": Yes")
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(label, color = Color.Red, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(value, color = Color.White, fontSize = 36.sp)
            }
        }
        // If "no", "unknown", or anything else → don't show at all
        return
    }

    // Default case: show normally
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(label + ":", color = Color.Red, fontSize = 28.sp)
        Text(value, color = Color.White, fontSize = 36.sp)
    }
}

