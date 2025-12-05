package com.example.a2048.Utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.a2048.Models.Score
import java.text.SimpleDateFormat
import java.util.*

class DB_Score(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ScoresDB"
        private const val TABLE_SCORES = "scores"

        private const val KEY_ID = "id"
        private const val KEY_POINTS = "points"
        private const val KEY_DATE = "date"
        private const val KEY_USER_NAME = "user_name"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_SCORES_TABLE = """
            CREATE TABLE $TABLE_SCORES (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_POINTS INTEGER NOT NULL,
                $KEY_DATE TEXT NOT NULL,
                $KEY_USER_NAME TEXT NOT NULL
            )
        """.trimIndent()
        db?.execSQL(CREATE_SCORES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SCORES")
        onCreate(db)
    }

    /**
     * CREATE - Insertar nuevo score
     */
    fun addScore(score: Score): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_POINTS, score.points)
            put(KEY_DATE, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(score.date))
            put(KEY_USER_NAME, score.userName)
        }
        val id = db.insert(TABLE_SCORES, null, values)
        db.close()
        return id
    }

    /**
     * READ - Obtener todos los scores
     */
    fun getAllScores(): List<Score> {
        val scoreList = mutableListOf<Score>()
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_SCORES ORDER BY $KEY_POINTS DESC"

        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val score = Score(
                    points = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_POINTS)),
                    date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                    ) ?: Date(),
                    userName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME))
                )
                scoreList.add(score)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return scoreList
    }

    /**
     * READ - Obtener score por ID
     */
    fun getScoreById(id: Int): Score? {
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_SCORES WHERE $KEY_ID = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val score = Score(
                points = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_POINTS)),
                date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                ) ?: Date(),
                userName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME))
            )
            cursor.close()
            db.close()
            score
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    /**
     * UPDATE - Actualizar score
     */
    fun updateScore(score: Score): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_POINTS, score.points)
            put(KEY_DATE, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(score.date))
            put(KEY_USER_NAME, score.userName)
        }
        val success = db.update(TABLE_SCORES, values, "$KEY_ID=?", arrayOf(score.points.toString())).toLong() > 0
        db.close()
        return success
    }

    /**
     * DELETE - Eliminar score por ID
     */
    fun deleteScore(id: Int): Boolean {
        val db = writableDatabase
        val success = db.delete(TABLE_SCORES, "$KEY_ID=?", arrayOf(id.toString())) > 0
        db.close()
        return success
    }

    /**
     * DELETE - Eliminar todos los scores
     */
    fun deleteAllScores(): Int {
        val db = writableDatabase
        val deleted = db.delete(TABLE_SCORES, null, null)
        db.close()
        return deleted
    }

    /**
     * Obtener número total de scores
     */
    fun getScoresCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_SCORES", null)
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        db.close()
        return count
    }
}
