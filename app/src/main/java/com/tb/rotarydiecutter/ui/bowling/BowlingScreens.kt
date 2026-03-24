package com.tb.rotarydiecutter.ui.bowling

import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tb.rotarydiecutter.R
import java.util.concurrent.TimeUnit

// ---------- Average Windows ----------
enum class AvgWindow(val label: String, val days: Int?) {
    LAST_10_GAMES("Last 10", null),
    WEEK("7d", 7),
    MONTH("30d", 30),
    THREE_MONTH("90d", 90),
    YEAR("365d", 365),
    ALL("All", null)
}

enum class BowlingAverageMode { BY_GAMES, BY_DAYS }

// ---------- HOME ----------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BowlingHomeScreen(nav: NavController) {
    val ctx = LocalContext.current
    val db = remember { BowlingDbHelper(ctx) }

    // Player selection
    var names by remember { mutableStateOf(listOf("All") + db.getNames()) }
    var selectedName by rememberSaveable { mutableStateOf(names.first()) }
    var nameMenu by remember { mutableStateOf(false) }

    var mode by rememberSaveable { mutableStateOf(BowlingAverageMode.BY_GAMES) }

    // Presets & custom values
    var gamesPreset by rememberSaveable { mutableStateOf<Int?>(10) }   // 10 or 20 or null (custom)
    var customGamesText by rememberSaveable { mutableStateOf("") }

    var daysPreset by rememberSaveable { mutableStateOf<Int?>(30) }    // 30 or null (custom)
    var customDaysText by rememberSaveable { mutableStateOf("") }

    // Parse helpers
    fun String.toPositiveIntOrNull(): Int? = this.toIntOrNull()?.takeIf { it >= 1 }

    val chosenGamesCount: Int? = when {
        mode == BowlingAverageMode.BY_GAMES && gamesPreset != null -> gamesPreset
        mode == BowlingAverageMode.BY_GAMES && gamesPreset == null -> customGamesText.toPositiveIntOrNull()
        else -> null
    }

    val chosenDaysCount: Int? = when {
        mode == BowlingAverageMode.BY_DAYS && daysPreset != null -> daysPreset
        mode == BowlingAverageMode.BY_DAYS && daysPreset == null -> customDaysText.toPositiveIntOrNull()
        else -> null
    }

    // Compute filtered set
    val games by remember(mode, selectedName, chosenGamesCount, chosenDaysCount) {
        mutableStateOf(
            when (mode) {
                BowlingAverageMode.BY_GAMES -> {
                    val all = db.getGames(name = selectedName)
                    val n = chosenGamesCount
                    if (n != null) all.take(n) else emptyList()
                }
                BowlingAverageMode.BY_DAYS -> {
                    val d = chosenDaysCount
                    if (d != null) {
                        val cutoff = System.currentTimeMillis() - java.util.concurrent.TimeUnit.DAYS.toMillis(d.toLong())
                        db.getGames(sinceMillis = cutoff, name = selectedName)
                    } else emptyList()
                }
            }
        )
    }

    val average: Int? = games.takeIf { it.isNotEmpty() }?.let { list ->
        (list.sumOf { it.score }.toDouble() / list.size).toInt()
    }

    // ---- UI ----
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.greenstripes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.65f)))

        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Bowling", style = MaterialTheme.typography.headlineMedium, color = Color.White)

            // Player dropdown
            ExposedDropdownMenuBox(expanded = nameMenu, onExpandedChange = { nameMenu = !nameMenu }) {
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Player", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(nameMenu) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                        unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = nameMenu, onDismissRequest = { nameMenu = false }) {
                    names.forEach { n ->
                        DropdownMenuItem(text = { Text(n) }, onClick = {
                            selectedName = n
                            nameMenu = false
                        })
                    }
                }
            }

            // Mode toggle
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == BowlingAverageMode.BY_GAMES,
                    onClick = { mode = BowlingAverageMode.BY_GAMES },
                    label = { Text("By games") }
                )
                FilterChip(
                    selected = mode == BowlingAverageMode.BY_DAYS,
                    onClick = { mode = BowlingAverageMode.BY_DAYS },
                    label = { Text("By days") }
                )
            }

            // Presets + custom fields
            if (mode == BowlingAverageMode.BY_GAMES) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = gamesPreset == 10,
                        onClick = { gamesPreset = 10; customGamesText = "" },
                        label = { Text("10 games") }
                    )
                    FilterChip(
                        selected = gamesPreset == 20,
                        onClick = { gamesPreset = 20; customGamesText = "" },
                        label = { Text("20 games") }
                    )
                    FilterChip(
                        selected = gamesPreset == null,
                        onClick = { gamesPreset = null },
                        label = { Text("Custom games") }
                    )
                }
                if (gamesPreset == null) {
                    OutlinedTextField(
                        value = customGamesText,
                        onValueChange = { customGamesText = it.filter(Char::isDigit) },
                        label = { Text("Number of games (≥1)", color = Color.White) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        isError = customGamesText.isNotEmpty() && customGamesText.toPositiveIntOrNull() == null,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                            unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = daysPreset == 30,
                        onClick = { daysPreset = 30; customDaysText = "" },
                        label = { Text("30 days") }
                    )
                    FilterChip(
                        selected = daysPreset == null,
                        onClick = { daysPreset = null },
                        label = { Text("Custom days") }
                    )
                }
                if (daysPreset == null) {
                    OutlinedTextField(
                        value = customDaysText,
                        onValueChange = { customDaysText = it.filter(Char::isDigit) },
                        label = { Text("Days (≥1)", color = Color.White) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        isError = customDaysText.isNotEmpty() && customDaysText.toPositiveIntOrNull() == null,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                            unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Average + actions
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF727272).copy(alpha = 0.75f))) {
                Column(Modifier.padding(16.dp)) {
                    Text("Average", color = Color.Red, fontWeight = FontWeight.Bold)
                    Text(average?.toString() ?: "--", color = Color.White, style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledTonalButton(onClick = { nav.navigate("bowl/add") }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp)); Text("Add Score")
                        }
                        OutlinedButton(onClick = { nav.navigate("bowl/history") }) {
                            Icon(Icons.Default.History, contentDescription = null)
                            Spacer(Modifier.width(6.dp)); Text("History")
                        }
                    }
                }
            }

            // Recent preview
            Text("Recent", color = Color.White, fontWeight = FontWeight.SemiBold)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(games.take(6)) { g -> BowlingRow(g) }
            }

            OutlinedButton(onClick = { nav.navigate("bowl/history") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Today, contentDescription = null)
                Spacer(Modifier.width(6.dp)); Text("See All")
            }
        }
    }
}

@Composable
private fun BowlingRow(g: BowlingDbHelper.Game) {
    val date = DateFormat.format("MMM d, yyyy  h:mm a", g.ts).toString()
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F))) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(g.name, color = Color(0xFF92C6FF), fontWeight = FontWeight.SemiBold)
                Text("Score", color = Color.Red)
                Text(g.score.toString(), color = Color.White, style = MaterialTheme.typography.headlineMedium)
            }
            Text(date, color = Color.White)
        }
    }
}

// ---------- ADD ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BowlingAddScreen(nav: NavController) {
    val ctx = LocalContext.current
    val db = remember { BowlingDbHelper(ctx) }
    val focus = LocalFocusManager.current

    var scoreText by rememberSaveable { mutableStateOf("") }
    var nameText by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var names by remember { mutableStateOf(db.getNames()) }

    val scoreInt = scoreText.toIntOrNull()
    val scoreValid = scoreInt != null && scoreInt in 0..300
    val canAdd = scoreValid && nameText.trim().isNotEmpty()
    val errorText = when {
        scoreText.isEmpty() -> null
        scoreInt == null -> "Enter a valid number"
        scoreInt !in 0..300 -> "Score must be 0–300"
        else -> null
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.yellowstripes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.65f)))

        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add Bowling Score", color = Color.White, style = MaterialTheme.typography.headlineMedium)

            // Name: type or pick
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Player Name", color = Color.White, fontWeight = FontWeight.Bold) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                        unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    if (names.isEmpty()) {
                        DropdownMenuItem(text = { Text("No players yet") }, onClick = { expanded = false })
                    } else {
                        names.forEach { n ->
                            DropdownMenuItem(text = { Text(n) }, onClick = {
                                nameText = n; expanded = false
                            })
                        }
                    }
                }
            }

            // Score: number keyboard + validation ≤ 300
            OutlinedTextField(
                value = scoreText,
                onValueChange = { new ->
                    // numeric-only (optional: allow empty)
                    val filtered = new.filter { it.isDigit() }
                    scoreText = filtered
                },
                label = { Text("Final Score (0–300)", color = Color.White, fontWeight = FontWeight.Bold) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                isError = errorText != null,
                supportingText = {
                    if (errorText != null) Text(errorText, color = Color(0xFFF4978E))
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                    unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { focus.clearFocus() }
                )
            )

            Button(
                onClick = {
                    val score = scoreInt!!
                    db.addScore(nameText, score, System.currentTimeMillis())
                    names = db.getNames()
                    // back to home
                    nav.navigate("bowl/home") { popUpTo(0); launchSingleTop = true }
                },
                enabled = canAdd,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF207318).copy(alpha = 0.90f)),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add", fontWeight = FontWeight.Bold) } // ← text says Add
        }
    }
}

// ---------- HISTORY ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BowlingHistoryScreen(nav: NavController) {
    val ctx = LocalContext.current
    val db = remember { BowlingDbHelper(ctx) }

    var names by remember { mutableStateOf(listOf("All") + db.getNames()) }
    var selectedName by rememberSaveable { mutableStateOf(names.first()) }
    var nameMenu by remember { mutableStateOf(false) }

    var games by remember(selectedName) { mutableStateOf(db.getGames(name = selectedName)) }
    fun refresh() {
        names = listOf("All") + db.getNames()
        games = db.getGames(name = selectedName)
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.blackwhite),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.90f)))

        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Bowling History", color = Color.White, style = MaterialTheme.typography.headlineMedium)

            ExposedDropdownMenuBox(expanded = nameMenu, onExpandedChange = { nameMenu = !nameMenu }) {
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Player", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(nameMenu) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                        unfocusedContainerColor = Color(0xFF4B4B4B).copy(alpha = 0.85f),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = nameMenu, onDismissRequest = { nameMenu = false }) {
                    names.forEach { n ->
                        DropdownMenuItem(text = { Text(n) }, onClick = {
                            selectedName = n
                            nameMenu = false
                            refresh()
                        })
                    }
                }
            }

            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(games, key = { it.id }) { g ->
                    val date = DateFormat.format("MMM d, yyyy  h:mm a", g.ts).toString()
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF727272).copy(alpha = 0.75f))) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(g.name, color = Color(0xFF92C6FF), fontWeight = FontWeight.SemiBold)
                                Text("Score", color = Color.Red)
                                Text(g.score.toString(), color = Color.White, style = MaterialTheme.typography.headlineSmall)
                                Text(date, color = Color.White)
                            }
                            IconButton(onClick = { db.deleteById(g.id); refresh() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFA60000))
                            }
                        }
                    }
                }
            }
        }
    }
}
