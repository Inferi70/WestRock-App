package com.tb.rotarydiecutter.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tb.rotarydiecutter.R
import com.tb.rotarydiecutter.models.RotaryView
import java.io.InputStream

@Composable
fun AddScreen(
    viewModel: RotaryView,
    navController: NavController,
    prefillDieCut: String = ""
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var f1 by remember { mutableStateOf(prefillDieCut) } // DieCut
    var f2 by remember { mutableStateOf("") } // BeltSpeed
    var f3 by remember { mutableStateOf("") } // TimeDelay
    var f4 by remember { mutableStateOf("") } // BPH
    var f5 by remember { mutableStateOf("") } // Customer
    var f6 by remember { mutableStateOf("") } // Notes
    var pullRoll by remember { mutableStateOf("") }
    var feedGate by remember { mutableStateOf("") }

    var wheelState by remember { mutableStateOf("Unknown") }
    var bundleBreaker by remember { mutableStateOf("Unknown") }
    var scissorLift by remember { mutableStateOf("Unknown") }
    var specialty by remember { mutableStateOf("Unknown") }

    val scroll = rememberScrollState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                val dbFile = context.getDatabasePath("rotary.db")
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(context, "Database loaded from file", Toast.LENGTH_LONG).show()
            }
        }
    )

    val fileSaverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri: Uri? ->
            uri?.let {
                val dbFile = context.getDatabasePath("rotary.db")
                val outputStream = context.contentResolver.openOutputStream(uri)
                outputStream?.use { dbFile.inputStream().copyTo(it) }
                Toast.makeText(context, "DB saved to: $uri", Toast.LENGTH_LONG).show()
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.yellowstripes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.65f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                f1, { f1 = it },
                label = { Text("Die Cut", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, lineHeight = 36.sp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Red
                )
            )
            OutlinedTextField(
                f4, { f4 = it },
                label = { Text("Box Per Hour", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, lineHeight = 36.sp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Red
                )
            )
            OutlinedTextField(
                f2, { f2 = it },
                label = { Text("Belt Speed", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, lineHeight = 36.sp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Red
                )
            )
            OutlinedTextField(
                f3, { f3 = it },
                label = { Text("Time Delay", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, lineHeight = 36.sp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Red
                )
            )
            OutlinedTextField(
                f6, { f6 = it },
                label = { Text("Notes", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, lineHeight = 36.sp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Red
                )
            )

            OutlinedTextField(
                pullRoll,
                { pullRoll = it },
                label = { Text("Pull Roll", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, lineHeight = 36.sp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Red
                )
            )
            OutlinedTextField(
                feedGate, { feedGate = it },
                label = { Text("Feed Gate", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, lineHeight = 36.sp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Red
                )
            )

            Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFC5C5C5).copy(alpha = 0.85f), thickness = 3.dp)

            Text("Wheels Position", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Up", "Down", "Unknown").forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = wheelState == option,
                            onClick = { wheelState = option })
                        Text(option, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFC5C5C5).copy(alpha = 0.85f), thickness = 3.dp)

            Text("Running Out", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Bundle Breaker", "Straight Out", "Unknown").forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = bundleBreaker == option,
                            onClick = { bundleBreaker = option })
                        Text(option, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFC5C5C5).copy(alpha = 0.85f), thickness = 3.dp)

            Text("Scissor Lift", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Yes", "Unknown").forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = scissorLift == option,
                            onClick = { scissorLift = option })
                        Text(option, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFC5C5C5).copy(alpha = 0.85f), thickness = 3.dp)

            Text("Skip Feed", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Yes", "No", "Unknown").forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = specialty == option,
                            onClick = { specialty = option })
                        Text(option, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFC5C5C5).copy(alpha = 0.85f), thickness = 3.dp)

            OutlinedTextField(
                f5, { f5 = it },
                label = { Text("Additional Notes", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, lineHeight = 36.sp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Red
                )
            )

            Button(onClick = {
                viewModel.addItem(
                    dieCut = f1.ifBlank { null },
                    beltSpeed = f2.ifBlank { null },
                    timeDelay = f3.ifBlank { null },
                    bph = f4.ifBlank { null },
                    customer = f5.ifBlank { null },
                    pullRoll = pullRoll.toDoubleOrNull(),
                    feedGate = feedGate.toDoubleOrNull(),
                    notes = f6.ifBlank { null },
                    wheels = wheelState,
                    bundleBreaker = bundleBreaker,
                    scissorLift = scissorLift,
                    specialty = specialty
                )
                f1 = ""; f2 = ""; f3 = ""; f4 = ""; f5 = ""; f6 = ""
                pullRoll = ""; feedGate = ""; wheelState = "Unknown"; bundleBreaker =
                "Unknown"; scissorLift = "Unknown"; specialty = "Unknown"
                Toast.makeText(context, "Item added successfully", Toast.LENGTH_SHORT).show()

                focusManager.clearFocus()

                // ✅ Go back to the Search screen
                if (!navController.popBackStack("search", inclusive = false)) {
                    // Fallback if "search" isn’t on the back stack for some reason
                    navController.navigate("search") {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
             },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF207318).copy(alpha = 0.90f),
                    contentColor = Color.White // optional: make text white
                )) {
                Text("Save/Add Cutting Die", fontWeight = FontWeight.Bold, fontSize = 28.sp)
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFA60000).copy(alpha = 0.85f), thickness = 5.dp)

            Button(onClick = {
                fileSaverLauncher.launch("rotary_backup.db")
            },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA60000).copy(alpha = 0.85f),
                    contentColor = Color.White // optional: make text white
                )) {
                Text("Save Contents to File", fontWeight = FontWeight.Bold)
            }

            Button(onClick = {
                filePickerLauncher.launch(arrayOf("*/*"))
            },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA60000).copy(alpha = 0.85f),
                    contentColor = Color.White // optional: make text white
                )) {
                Text("Load Contents from File", fontWeight = FontWeight.Bold)
            }
        }
    }
}
