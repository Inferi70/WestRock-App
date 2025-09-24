package com.tb.rotarydiecutter.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RotaryDatabase(context: Context) : SQLiteOpenHelper(context, "rotary.db", null, 4) {
    //Customer is aditional notes now, Specialty is Skip feed now
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE rotary_db (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                DieCut TEXT,
                BeltSpeed TEXT,
                PullRoll REAL,
                FeedGate REAL,
                TimeDelay TEXT,
                BPH TEXT,
                Customer TEXT,
                notes TEXT,
                Wheels TEXT,
                BundleBreaker TEXT DEFAULT 'No',
                ScissorLift TEXT DEFAULT 'No',
                Specialty TEXT DEFAULT 'No',
                last_accessed INTEGER DEFAULT (strftime('%s', 'now') - strftime('%s', '2025-01-01 00:00:00')),
                accessed_count INTEGER DEFAULT 0
            );
            """.trimIndent()
        )

        // App-wide scratch notes table
        createScratchNotesTableIfMissing(db)

        // Only gets run once ever
        populate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 3) {
                addColumnIfNotExists(db, "rotary_db", "last_accessed", "INTEGER")
                addColumnIfNotExists(db, "rotary_db", "accessed_count", "INTEGER")
            }
            if (oldVersion < 4) {
                createScratchNotesTableIfMissing(db)
            }
    }

    private fun createScratchNotesTableIfMissing(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS scratch_notes (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                json TEXT NOT NULL
            );
            """.trimIndent()
        )
        // Ensure a single default row exists
        db.execSQL(
            """
            INSERT INTO scratch_notes (id, json)
            SELECT 1, '[
    {
        "layout": {
            "marginH": 50,
            "paddingH": 20,
            "paddingV": 10
        }
    },
    {
        "stack": {
            "lineMult": 0.9,
            "items": [
                {
                    "columns": [
                        {
                            "text": "Pull Roll",
                            "size": 28,
                            "bold": true,
                            "color": "#FF7043"
                        },
                        {
                            "text": "Feed Gate",
                            "size": 28,
                            "bold": true,
                            "color": "#FF7043"
                        }
                    ]
                },
                {
                    "columns": [
                        {
                            "text": "C: .103",
                            "size": 19,
                            "color": "yellow",
                            "bold": true
                        },
                        {
                            "text": ".284",
                            "size": 19,
                            "color": "yellow",
                            "bold": true
                        }
                    ]
                },
                {
                    "columns": [
                        {
                            "text": "B: .074",
                            "size": 19,
                            "color": "red",
                            "bold": true
                        },
                        {
                            "text": ".181",
                            "size": 19,
                            "color": "red",
                            "bold": true
                        }
                    ]
                },
                {
                    "columns": [
                        {
                            "text": "E: .009",
                            "size": 19,
                            "color": "blue",
                            "bold": true
                        },
                        {
                            "text": ".118",
                            "size": 19,
                            "color": "blue",
                            "bold": true
                        }
                    ]
                },
                {
                    "columns": [
                        {
                            "text": "BC: .22",
                            "size": 19,
                            "bold": true
                        },
                        {
                            "text": ".31",
                            "size": 19,
                            "bold": true
                        }
                    ]
                }
            ]
        }
    },
    {
        "newline": 10
    },
    {
        "stack": {
            "lineMult": 0.6,
            "items": [
                {
                    "text": "Nip & Snub Wheels",
                    "size": 20,
                    "color": "grey",
                    "bold": true
                },
                {
                    "text": "C: 150",
                    "color": "grey"
                },
                {
                    "text": "B: 120",
                    "color": "grey"
                },
                {
                    "text": "E: 106",
                    "color": "grey"
                },
                {
                    "text": "BC: 250",
                    "color": "grey"
                }
            ]
        }
    }
]'
            WHERE NOT EXISTS (SELECT 1 FROM scratch_notes WHERE id = 1);
            """.trimIndent()
        )
    }

    fun addColumnIfNotExists(db: SQLiteDatabase, table: String, column: String, definition: String) {
        val cursor = db.rawQuery("PRAGMA table_info($table)", null)
        cursor.use {
            while (it.moveToNext()) {
                if (it.getString(it.getColumnIndexOrThrow("name")) == column) return
            }
        }
        db.execSQL("ALTER TABLE $table ADD COLUMN $column $definition")
    }

    fun populate(db: SQLiteDatabase = writableDatabase) {
        //Example of how to add default data.
//        db.execSQL(
//            """
//            INSERT INTO rotary_db (DieCut, BeltSpeed, PullRoll, FeedGate, TimeDelay, BPH, Customer, notes, Wheels, BundleBreaker, ScissorLift, Specialty) VALUES
//                ("3698", "143 - 28", NULL, NULL, "8/2 Sec.", NULL, NULL, "101
//263", "Down", "No", "No", "No"),
//                ("5065", "123 - (39 or 54)
//Steve: 123 - 41", 74.0, 172.0, "10/5 Sec.
//Steve:10/2 Sec.", "6500", "Great Lakes", '16.5" Snub Wheels
//Acc. 80% Wheel Down', "Down", "No", "No", "No")
//            """.trimIndent()
//        )
    }

    // ----------------------------
    // Scratch Notes: Read / Write
    // ----------------------------

    /**
     * Returns the JSON string stored in scratch_notes.id=1.
     * If missing, inserts and returns "[]".
     */
    fun getScratchNotes(db: SQLiteDatabase = readableDatabase): String {
        readableDatabase // ensure open
        val c = db.rawQuery("SELECT json FROM scratch_notes WHERE id = 1", null)
        c.use {
            if (it.moveToFirst()) {
                return it.getString(0) ?: "[]"
            }
        }
        // If row somehow missing, create it and return default
        saveScratchNotes("[]", writableDatabase)
        return "[]"
    }

    /**
     * Saves (upserts) the JSON into scratch_notes.id=1.
     */
    fun saveScratchNotes(json: String, db: SQLiteDatabase = writableDatabase) {
        val values = ContentValues().apply { put("json", json) }
        val updated = db.update("scratch_notes", values, "id = 1", null)
        if (updated == 0) {
            values.put("id", 1)
            db.insert("scratch_notes", null, values)
        }
    }
}