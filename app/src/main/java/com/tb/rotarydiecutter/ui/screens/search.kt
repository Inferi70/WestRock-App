package com.tb.rotarydiecutter.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tb.rotarydiecutter.R
import com.tb.rotarydiecutter.models.RotaryView
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

@Composable
fun LabelValue(label: String, value: String?) {
    Column(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(text = "$label:", color = Color.Red, fontSize = 18.sp)
        Text(text = value ?: "", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
fun SearchScreen(viewModel: RotaryView, navController: NavController) {
    var query by rememberSaveable { mutableStateOf("") }
    var numericMode by rememberSaveable { mutableStateOf(true) } // false=text, true=number
    val focusManager = LocalFocusManager.current

    val results by remember(query) {
        mutableStateOf(if (query.isNotEmpty()) viewModel.search(query) else emptyList())
    }

    val notesJson by viewModel.scratchNotes

    // Parse notes (supports: layout, text, columns, stack(text), stack(rows with columns), fractional newline)
    var parseError by rememberSaveable { mutableStateOf<String?>(null) }
    val parsed = remember(notesJson) {
        try {
            parseError = null
            parseRichNotes(notesJson)
        } catch (e: Exception) {
            parseError = e.message
            ParsedNotes(NotesLayout(), emptyList())
        }
    }

    // Edit dialog state (quad tap)
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var draftJson by rememberSaveable { mutableStateOf(notesJson) }
    var draftError by rememberSaveable { mutableStateOf<String?>(null) }

    // Quad-tap to edit
    var tapCount by rememberSaveable { mutableStateOf(0) }
    var lastTapTimeMs by rememberSaveable { mutableStateOf(0L) }
    fun onPreviewTapped() {
        val now = System.currentTimeMillis()
        tapCount = if (now - lastTapTimeMs <= 500L) tapCount + 1 else 1
        lastTapTimeMs = now
        if (tapCount >= 4) {
            tapCount = 0
            draftJson = notesJson
            draftError = null
            showEditor = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.stripesverticle),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )

        // Column so we can pin search at top and bottom-stick the preview when idle
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // Search input with keyboard toggle
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search", fontSize = 26.sp, color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (numericMode) KeyboardType.Number else KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                trailingIcon = {
                    IconButton(onClick = { numericMode = !numericMode }) {
                        val showTextNext = !numericMode
                        Icon(
                            imageVector = if (showTextNext) Icons.Filled.Keyboard else Icons.Filled.Dialpad,
                            contentDescription = if (showTextNext) "Switch to text keyboard" else "Switch to number keyboard",
                            tint = Color.Red
                        )
                    }
                },
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

            if (query.isBlank()) {
                // No results list; push preview to the bottom
                Spacer(Modifier.weight(1f))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = parsed.layout.marginH.dp)   // from JSON layout
                        .clickable { onPreviewTapped() },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Column(
                        Modifier.padding(
                            horizontal = parsed.layout.paddingH.dp,        // from JSON layout
                            vertical   = parsed.layout.paddingV.dp         // from JSON layout
                        )
                    ) {
                        RichNotesPreview(parsed.blocks)
                        if (parseError != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "JSON error: $parseError",
                                color = Color(0xFFFF5252),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                // Results or "Add" prompt
                if (results.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E),
                            contentColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No results for \"$query\"",
                                fontSize = 18.sp,
                                color = Color(0xFFFF7043),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Add a new die with this name?",
                                fontSize = 16.sp,
                                color = Color(0xFFB0BEC5)
                            )
                            Button(
                                onClick = {
                                    val encoded = Uri.encode(query)
                                    navController.navigate("rotary_add?dieCut=$encoded")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDA4717),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Add \"$query\"", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
        }

        /* ---------- Edit JSON Dialog ---------- */
        if (showEditor) {
            AlertDialog(
                onDismissRequest = { showEditor = false },
                title = { Text("Edit Notes JSON") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = draftJson,
                            onValueChange = {
                                draftJson = it
                                draftError = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, lineHeight = 22.sp),
                            minLines = 8,
                            maxLines = 12,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { /* keep open */ }),
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
                        if (draftError != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(draftError!!, color = Color(0xFFFF5252), fontSize = 14.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        try {
                            // Validate by parsing
                            parseRichNotes(draftJson)
                            viewModel.saveScratchNotes(draftJson) // persist to SQLite (scratch_notes)
                            showEditor = false
                        } catch (e: Exception) {
                            draftError = e.message ?: "Invalid JSON"
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditor = false }) { Text("Cancel") }
                }
            )
        }
    }
}

/* ---------- Rendering blocks ---------- */

private sealed class RichBlock {
    data class TextBlock(val content: AnnotatedString) : RichBlock()
    data class ColumnsBlock(val left: AnnotatedString, val right: AnnotatedString) : RichBlock()
    data class NewlineBlock(val amount: Float) : RichBlock()
    /** Stack of text-only lines rendered in a single Text with custom line height. */
    data class StackBlock(val content: AnnotatedString, val lineHeightSp: Float?) : RichBlock()
}

/** Rows that can live inside a stack: either a single line of text or a 2-column line. */
private sealed class StackRow {
    data class Text(val content: AnnotatedString) : StackRow()
    data class Columns(val left: AnnotatedString, val right: AnnotatedString) : StackRow()
}

/** A stack that contains rows (text OR columns). rowMult controls vertical gap (can be < 1) */
private data class StackRowsBlock(
    val rows: List<StackRow>,
    val rowMult: Float? // if null, uses default gap (1f)
) : RichBlock()

private data class NotesLayout(
    val marginH: Int = 0,
    val paddingH: Int = 12,
    val paddingV: Int = 6
)
private data class ParsedNotes(
    val layout: NotesLayout,
    val blocks: List<RichBlock>
)

@Composable
private fun RichNotesPreview(blocks: List<RichBlock>) {
    val BASE = 4.dp  // base unit for fractional newline

    Column {
        blocks.forEach { block ->
            when (block) {
                is RichBlock.TextBlock -> Text(block.content)

                is RichBlock.ColumnsBlock -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(block.left)
                    Text(block.right)
                }

                is RichBlock.NewlineBlock -> {
                    Spacer(Modifier.height(BASE * block.amount))
                }

                is RichBlock.StackBlock -> {
                    Text(
                        text = block.content,
                        style = LocalTextStyle.current.copy(
                            lineHeight = block.lineHeightSp?.sp
                                ?: androidx.compose.ui.unit.TextUnit.Unspecified
                        )
                    )
                }

                is StackRowsBlock -> {
                    TightStackRows(block.rows, rowMult = block.rowMult ?: 1f)
                }
            }
        }
    }
}

/**
 * Custom layout that can pack rows closer than a single line by overlapping them.
 * rowMult < 1f = tighter (overlap), 1f = normal, >1f = extra spacing.
 */
@Composable
private fun TightStackRows(rows: List<StackRow>, rowMult: Float) {
    Layout(
        content = {
            rows.forEach { row ->
                when (row) {
                    is StackRow.Text -> Text(row.content)
                    is StackRow.Columns -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(row.left)
                        Text(row.right)
                    }
                }
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minHeight = 0)) }

        var y = 0
        var lastStartY = 0
        var lastH = 0

        placeables.forEachIndexed { idx, p ->
            val startY = y
            y += (p.height * rowMult).roundToInt()
            if (idx == placeables.lastIndex) {
                lastStartY = startY
                lastH = p.height
            }
        }
        val totalHeight = if (placeables.isEmpty()) 0 else lastStartY + lastH

        layout(constraints.maxWidth, totalHeight) {
            var yy = 0
            placeables.forEach { p ->
                p.place(x = 0, y = yy)
                yy += (p.height * rowMult).roundToInt()
            }
        }
    }
}

/* ---------- Parser ---------- */
private fun parseRichNotes(json: String): ParsedNotes {
    val arr = JSONArray(json)
    var layout = NotesLayout()
    val blocks = mutableListOf<RichBlock>()

    fun parseColor(s: String?): Color? {
        if (s.isNullOrBlank()) return null
        return when (s.lowercase()) {
            "red" -> Color.Red
            "green" -> Color(0xFF4CAF50)
            "blue" -> Color(0xFF2196F3)
            "white" -> Color.White
            "black" -> Color.Black
            "gray","grey" -> Color.Gray
            "yellow" -> Color(0xFFFFEB3B)
            "cyan" -> Color(0xFF00BCD4)
            "magenta" -> Color(0xFFE91E63)
            else -> {
                val clean = s.removePrefix("#")
                val value = when (clean.length) {
                    6 -> 0xFF000000 or clean.toLong(16)
                    8 -> clean.toLong(16)
                    else -> throw IllegalArgumentException("Bad color: $s")
                }
                Color(value.toULong().toLong())
            }
        }
    }

    fun makeSpan(obj: JSONObject): AnnotatedString {
        val text = obj.optString("text", "")
        val size = obj.optDouble("size", Double.NaN)
        val bold = obj.optBoolean("bold", false)
        val italic = obj.optBoolean("italic", false)
        val color = parseColor(obj.optString("color", null))
        val style = SpanStyle(
            color = color ?: Color.Unspecified,
            fontSize = if (!size.isNaN()) size.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
            fontWeight = if (bold) FontWeight.Bold else null,
            fontStyle = if (italic) FontStyle.Italic else null
        )
        return AnnotatedString.Builder().apply { withStyle(style) { append(text) } }.toAnnotatedString()
    }

    fun sizeOf(obj: JSONObject): Double =
        obj.optDouble("size", Double.NaN).let { if (it.isNaN()) Double.NaN else it }

    for (i in 0 until arr.length()) {
        val node = arr.get(i)
        require(node is JSONObject) { "Array items must be objects" }

        // Optional global layout
        if (node.has("layout")) {
            val lo = node.getJSONObject("layout")
            layout = layout.copy(
                marginH  = lo.optInt("marginH",  layout.marginH),
                paddingH = lo.optInt("paddingH", layout.paddingH),
                paddingV = lo.optInt("paddingV", layout.paddingV)
            )
            continue
        }

        // Fractional newline spacing
        if (node.has("newline")) {
            val amt = node.optDouble("newline", 1.0).toFloat().coerceAtLeast(0f)
            blocks += RichBlock.NewlineBlock(amt)
            continue
        }

        // Two-column block
        if (node.has("columns")) {
            val cols = node.getJSONArray("columns")
            require(cols.length() == 2) { "`columns` must have exactly 2 items" }
            val leftObj = cols.getJSONObject(0)
            val rightObj = cols.getJSONObject(1)
            blocks += RichBlock.ColumnsBlock(
                left = makeSpan(leftObj),
                right = makeSpan(rightObj)
            )
            continue
        }

        // NEW: Stack that can be either text-only (single Text) OR rows (Text/Columns per row)
        if (node.has("stack")) {
            val stack = node.getJSONObject("stack")
            val items = stack.getJSONArray("items")

            // detect whether any item is a columns object
            var hasColumns = false
            for (j in 0 until items.length()) {
                if (items.getJSONObject(j).has("columns")) { hasColumns = true; break }
            }

            if (hasColumns) {
                // Build a list of StackRow (Text or Columns)
                val rows = mutableListOf<StackRow>()
                for (j in 0 until items.length()) {
                    val item = items.getJSONObject(j)
                    if (item.has("columns")) {
                        val cols = item.getJSONArray("columns")
                        require(cols.length() == 2) { "`columns` inside stack must have exactly 2 items" }
                        val l = makeSpan(cols.getJSONObject(0))
                        val r = makeSpan(cols.getJSONObject(1))
                        rows += StackRow.Columns(l, r)
                    } else {
                        // treat as a normal text object inside the stack
                        rows += StackRow.Text(makeSpan(item))
                    }
                }
                // Map "lineMult" (or "rowMult") to row spacing multiplier (supports < 1)
                val rowMult = when {
                    stack.has("rowMult") -> stack.optDouble("rowMult", 1.0).toFloat()
                    stack.has("lineMult") -> stack.optDouble("lineMult", 1.0).toFloat()
                    else -> 1f
                }
                blocks += StackRowsBlock(rows, rowMult)
            } else {
                // text-only stack -> single Text with custom line height
                val builder = AnnotatedString.Builder()
                var maxSizeSp = 16.0

                for (j in 0 until items.length()) {
                    val item = items.getJSONObject(j)
                    val span = makeSpan(item)
                    val sz = sizeOf(item)
                    if (!sz.isNaN()) maxSizeSp = maxOf(maxSizeSp, sz)
                    builder.append(span)
                    if (j < items.length() - 1) builder.append("\n")
                }

                val lineHeightSp: Float? = when {
                    stack.has("lineHeight") -> stack.optDouble("lineHeight").toFloat()
                    stack.has("lineMult") -> (maxSizeSp * stack.optDouble("lineMult", 1.0)).toFloat()
                    else -> null
                }

                blocks += RichBlock.StackBlock(builder.toAnnotatedString(), lineHeightSp)
            }
            continue
        }

        // Default: single text
        blocks += RichBlock.TextBlock(makeSpan(node))
    }

    return ParsedNotes(layout, blocks)
}

// Backward-compat wrapper if something else still calls it:
private fun parseRichJsonBlocks(json: String): List<RichBlock> = parseRichNotes(json).blocks
