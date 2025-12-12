package com.example.a2048.Utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.a2048.Models.Score
import java.text.SimpleDateFormat
import java.util.*

/**
 *  gestionar la base de datos de puntuaciones del juego 2048.
 *
 * Se encarga de crear y actualizar el esquema, así como de ofrecer
 * operaciones CRUD sobre la tabla de puntuaciones.
 */
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

    /**
     * Crea la tabla de puntuaciones en la base de datos.
     *
     * Este metodo se ejecuta automáticamente cuando la base de datos
     * se crea por primera vez.
     *
     * @param db instancia de [SQLiteDatabase] sobre la que se ejecuta el SQL de creación.
     */
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

    /**
     * Actualiza el esquema de la base de datos cuando cambia la versión.
     *
     * En este caso se elimina la tabla existente y se vuelve a crear.
     *
     * @param db instancia de [SQLiteDatabase] a actualizar.
     * @param oldVersion versión anterior de la base de datos.
     * @param newVersion nueva versión de la base de datos.
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SCORES")
        onCreate(db)
    }

    /**
     * Inserta una nueva puntuación en la base de datos.
     *
     * @param score objeto [Score] que contiene puntos, fecha y nombre de usuario.
     * @return identificador autogenerado de la fila insertada, o -1 si falla.
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
     * Obtiene todas las puntuaciones almacenadas ordenadas de mayor a menor puntuación.
     *
     * @return lista de objetos [Score] con todas las puntuaciones existentes.
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
     * Obtiene una puntuación concreta a partir de su identificador.
     *
     * @param id identificador de la puntuación a recuperar.
     * @return objeto [Score] si existe una fila con ese id, o `null` en caso contrario.
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
     * Actualiza los datos de una puntuación existente.
     *
     * Importante: el identificador usado en la cláusula `WHERE` debería ser el id de la fila,
     * no los puntos (ahora mismo se usa `score.points`).
     *
     * @param score objeto [Score] con los nuevos valores a guardar.
     * @return `true` si se ha actualizado al menos una fila, `false` en caso contrario.
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
     * Elimina una puntuación concreta por su identificador.
     *
     * @param id identificador de la puntuación a eliminar.
     * @return `true` si se ha eliminado al menos una fila, `false` en caso contrario.
     */
    fun deleteScore(id: Int): Boolean {
        val db = writableDatabase
        val success = db.delete(TABLE_SCORES, "$KEY_ID=?", arrayOf(id.toString())) > 0
        db.close()
        return success
    }

    /**
     * Elimina todas las puntuaciones de la tabla.
     *
     * @return número de filas eliminadas.
     */
    fun deleteAllScores(): Int {
        val db = writableDatabase
        val deleted = db.delete(TABLE_SCORES, null, null)
        db.close()
        return deleted
    }

    /**
     * Obtiene el número total de puntuaciones almacenadas.
     *
     * @return número de filas en la tabla de puntuaciones.
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
