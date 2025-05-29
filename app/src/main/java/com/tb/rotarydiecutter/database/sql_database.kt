package com.tb.rotarydiecutter.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RotaryDatabase(context: Context) : SQLiteOpenHelper(context, "rotary.db", null, 3) {
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

        // Only gets run once ever
        populate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 3) {
                addColumnIfNotExists(db, "rotary_db", "last_accessed", "INTEGER")
                addColumnIfNotExists(db, "rotary_db", "accessed_count", "INTEGER")
            }
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
}