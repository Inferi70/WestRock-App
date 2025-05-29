package com.tb.rotarydiecutter.models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.tb.rotarydiecutter.database.RotaryDatabase

data class DummyItem(
    val id: Int,
    val DieCut: String?,
    val BeltSpeed: String?,
    val PullRoll: Double?,
    val FeedGate: Double?,
    val TimeDelay: String?,
    val BPH: String?,
    val Customer: String?,
    val Notes: String?,
    val Wheels: String?,
    val BundleBreaker: String?,
    val ScissorLift: String?,
    val Specialty: String?,
    val accessedCount: Int? = null
)

class RotaryView(context: Context) : ViewModel() {
    private val dbHelper = RotaryDatabase(context)

    val dummyItems = mutableStateListOf<DummyItem>()

    init {
        refresh()
    }

    fun refresh() {
        dummyItems.clear()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM rotary_db", null)
        while (cursor.moveToNext()) {
            dummyItems.add(
                DummyItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DieCut")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BeltSpeed")),
                    cursor.getDoubleOrNull("PullRoll"),
                    cursor.getDoubleOrNull("FeedGate"),
                    cursor.getString(cursor.getColumnIndexOrThrow("TimeDelay")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BPH")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Customer")),
                    cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Wheels")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BundleBreaker")),
                    cursor.getString(cursor.getColumnIndexOrThrow("ScissorLift")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Specialty")),
                    cursor.getIntOrNull("accessed_count")
                )
            )
        }
        cursor.close()
    }

    fun addItem(
        dieCut: String?, beltSpeed: String?, timeDelay: String?, bph: String?, customer: String?,
        pullRoll: Double?, feedGate: Double?, notes: String?,
        wheels: String?, bundleBreaker: String?, scissorLift: String?, specialty: String?
    ) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("DieCut", dieCut)
            put("BeltSpeed", beltSpeed)
            put("TimeDelay", timeDelay)
            put("BPH", bph)
            put("Customer", customer)
            put("PullRoll", pullRoll)
            put("FeedGate", feedGate)
            put("notes", notes)
            put("Wheels", wheels)
            put("BundleBreaker", bundleBreaker)
            put("ScissorLift", scissorLift)
            put("Specialty", specialty)
        }
        db.insert("rotary_db", null, values)
        refresh()
    }

    fun deleteById(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete("rotary_db", "id = ?", arrayOf(id.toString()))
        refresh()
    }

    fun updateItem(
        id: Int,
        dieCut: String?, beltSpeed: String?, timeDelay: String?, bph: String?, customer: String?,
        pullRoll: Double?, feedGate: Double?, notes: String?, wheels: String?,
        bundleBreaker: String?, scissorLift: String?, specialty: String?
    ) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("DieCut", dieCut)
            put("BeltSpeed", beltSpeed)
            put("TimeDelay", timeDelay)
            put("BPH", bph)
            put("Customer", customer)
            put("PullRoll", pullRoll)
            put("FeedGate", feedGate)
            put("notes", notes)
            put("Wheels", wheels)
            put("BundleBreaker", bundleBreaker)
            put("ScissorLift", scissorLift)
            put("Specialty", specialty)
        }
        db.update("rotary_db", values, "id = ?", arrayOf(id.toString()))
        refresh()
    }


    fun search(query: String): List<DummyItem> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT * FROM rotary_db
                WHERE DieCut LIKE ? OR Customer LIKE ? OR notes LIKE ?
            """.trimIndent(),
            arrayOf("%$query%", "%$query%", "%$query%")
        )
        val results = mutableListOf<DummyItem>()
        while (cursor.moveToNext()) {
            results.add(
                DummyItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DieCut")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BeltSpeed")),
                    cursor.getDoubleOrNull("PullRoll"),
                    cursor.getDoubleOrNull("FeedGate"),
                    cursor.getString(cursor.getColumnIndexOrThrow("TimeDelay")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BPH")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Customer")),
                    cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Wheels")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BundleBreaker")),
                    cursor.getString(cursor.getColumnIndexOrThrow("ScissorLift")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Specialty"))
                )
            )
        }
        cursor.close()
        return results
    }

    fun updateLastAccessed(id: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("last_accessed", System.currentTimeMillis() / 1000) // seconds
        }
        db.update("rotary_db", values, "id = ?", arrayOf(id.toString()))
    }

    fun incrementAccessedCount(id: Int) {
        Log.d("RotaryView", "Running incrementAccessedCount for id=$id") // ✅ log here
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE rotary_db SET accessed_count = COALESCE(accessed_count, 0) + 1 WHERE id = ?",
            arrayOf(id)
        )
    }

    fun resetAccessedCount(id: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("accessed_count", 0)
        }
        db.update("rotary_db", values, "id = ?", arrayOf(id.toString()))
        refresh() // optional: only if you need to reflect changes in a list
    }

    //Updated limit according to needs
    fun getLastAccessedItems(limit: Int = 10): List<DummyItem> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT * FROM rotary_db
        WHERE last_accessed IS NOT NULL
        ORDER BY last_accessed DESC
        LIMIT ?
        """.trimIndent(),
            arrayOf(limit.toString())
        )
        val results = mutableListOf<DummyItem>()
        while (cursor.moveToNext()) {
            results.add(
                DummyItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DieCut")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BeltSpeed")),
                    cursor.getDoubleOrNull("PullRoll"),
                    cursor.getDoubleOrNull("FeedGate"),
                    cursor.getString(cursor.getColumnIndexOrThrow("TimeDelay")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BPH")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Customer")),
                    cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Wheels")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BundleBreaker")),
                    cursor.getString(cursor.getColumnIndexOrThrow("ScissorLift")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Specialty")),
                    cursor.getIntOrNull("accessed_count")
                )
            )
        }
        cursor.close()
        return results
    }

    fun getMostAccessedItems(limit: Int = 10): List<DummyItem> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT * FROM rotary_db
        WHERE accessed_count IS NOT NULL
        ORDER BY accessed_count DESC
        LIMIT ?
        """.trimIndent(),
            arrayOf(limit.toString())
        )

        val results = mutableListOf<DummyItem>()
        while (cursor.moveToNext()) {
            results.add(
                DummyItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DieCut")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BeltSpeed")),
                    cursor.getDoubleOrNull("PullRoll"),
                    cursor.getDoubleOrNull("FeedGate"),
                    cursor.getString(cursor.getColumnIndexOrThrow("TimeDelay")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BPH")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Customer")),
                    cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Wheels")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BundleBreaker")),
                    cursor.getString(cursor.getColumnIndexOrThrow("ScissorLift")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Specialty")),
                    cursor.getIntOrNull("accessed_count")
                )
            )
        }
        cursor.close()
        return results
    }
}

// Extension function to safely get nullable Doubles from a cursor
fun Cursor.getDoubleOrNull(columnName: String): Double? {
    val idx = getColumnIndex(columnName)
    return if (isNull(idx)) null else getDouble(idx)
}

fun Cursor.getIntOrNull(columnName: String): Int? {
    val idx = getColumnIndex(columnName)
    return if (isNull(idx)) null else getInt(idx)
}
