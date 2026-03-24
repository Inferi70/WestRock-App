package com.tb.rotarydiecutter.ui.bowling

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteException

// Standalone DB just for Bowling (no touchy rotary.db)
private const val DB_NAME = "bowling.db"
private const val DB_VERSION = 1
private const val T_GAMES = "bowling_games"

class BowlingDbHelper(ctx: Context) : SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        createSchema(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        createSchema(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        createSchema(db) // ensure table exists
    }

    private fun createSchema(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $T_GAMES(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                score INTEGER NOT NULL,
                ts INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_${T_GAMES}_name_ts ON $T_GAMES(name, ts)")
    }

    data class Game(val id: Int, val name: String, val score: Int, val ts: Long)

    fun addScore(name: String, score: Int, timestampMillis: Long): Long {
        writableDatabase.use { w ->
            val cv = ContentValues().apply {
                put("name", name.trim().ifEmpty { "Player" })
                put("score", score)
                put("ts", timestampMillis)
            }
            return w.insert(T_GAMES, null, cv)
        }
    }

    fun getNames(): List<String> = readableDatabase.use { r ->
        r.rawQuery(
            "SELECT name, MAX(ts) AS last_ts FROM $T_GAMES GROUP BY name ORDER BY last_ts DESC",
            emptyArray()
        ).use { c ->
            val out = ArrayList<String>(c.count.coerceAtLeast(0))
            while (c.moveToNext()) out += c.getString(0)
            out
        }
    }

    fun getGames(sinceMillis: Long? = null, name: String? = null): List<Game> {
        try {
            return queryGamesInternal(sinceMillis, name)
        } catch (e: SQLiteException) {
            if (e.message?.contains("no such table", true) == true) {
                writableDatabase.use { w -> createSchema(w) }
                return queryGamesInternal(sinceMillis, name)
            }
            throw e
        }
    }

    private fun queryGamesInternal(sinceMillis: Long?, name: String?): List<Game> {
        return readableDatabase.use { r ->
            val whereParts = mutableListOf<String>()
            val args = mutableListOf<String>()
            if (sinceMillis != null) {
                whereParts += "ts >= ?"
                args += sinceMillis.toString()
            }
            if (!name.isNullOrBlank() && name != "All") {
                whereParts += "name = ?"
                args += name
            }
            val where = if (whereParts.isEmpty()) "" else "WHERE ${whereParts.joinToString(" AND ")}"

            r.rawQuery(
                "SELECT id, name, score, ts FROM $T_GAMES $where ORDER BY ts DESC",
                args.toTypedArray()
            ).use { c ->
                val out = ArrayList<Game>(c.count.coerceAtLeast(0))
                while (c.moveToNext()) {
                    out += Game(
                        id = c.getInt(0),
                        name = c.getString(1),
                        score = c.getInt(2),
                        ts = c.getLong(3),
                    )
                }
                out
            }
        }
    }


    fun deleteById(id: Int) {
        writableDatabase.use { w ->
            w.delete(T_GAMES, "id = ?", arrayOf(id.toString()))
        }
    }
}
