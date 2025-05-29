package com.tb.rotarydiecutter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tb.rotarydiecutter.R
import com.tb.rotarydiecutter.models.RotaryView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbScreen(viewModel: RotaryView, navController: NavController) {
    var showConfirmDialogFor by remember { mutableStateOf<Int?>(null) }

    var viewMode by remember { mutableStateOf("Recently Accessed") }
    val options = listOf("Recently Accessed", "Counts", "All Dies")
    var expanded by remember { mutableStateOf(false) }



    val itemsToDisplay = when (viewMode) {
        "All Dies" -> viewModel.dummyItems
            .filter { it.DieCut != null }
            .sortedBy { it.DieCut?.lowercase() }
        "Counts" -> viewModel.getMostAccessedItems()
        else -> viewModel.getLastAccessedItems()
    }



    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.greenstripes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.65f))
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // View mode dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = viewMode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select View Mode", fontSize = 20.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1F1F1F),
                            unfocusedContainerColor = Color(0xFF1F1F1F),
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = 28.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    viewMode = option
                                    expanded = false
                                }
                            )

                            if (index != options.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp)) // adjust as needed
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            items(itemsToDisplay) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF727272).copy(alpha = 0.75f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.DieCut ?: "(Unnamed)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    navController.navigate("rotary_detail/${item.id}")
                                }
                        )
                        if (viewMode == "Counts") {
                            LabelValue("Count:", item.accessedCount?.toString() ?: "0")
                        }

                        if (viewMode == "All Dies") {
                            IconButton(onClick = { showConfirmDialogFor = item.id }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFA60000),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                if (showConfirmDialogFor == item.id) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDialogFor = null },
                        title = { Text("Confirm Delete") },
                        text = { Text("Are you sure you want to delete this item?") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteById(item.id)
                                showConfirmDialogFor = null
                            }) {
                                Text("Delete", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmDialogFor = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}
