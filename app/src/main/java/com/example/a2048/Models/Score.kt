package com.example.a2048.Models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Score(
    var points: Int,
    var date: Date = Date(),
    var userName: String = ""
) {
    // Constructor vacío (por defecto)
    constructor() : this(0, Date(), "")

    // Formato
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    override fun toString(): String {
        return "Score(points=$points, date=${getFormattedDate()}, user=$userName)"
    }

    /**
     * Patron factory method.
     * companion object declara el bloque estático asociado a la clase Score.
     * createDefault es una función de fábrica que crea una instancia de Score con:
     *  - Puntuación inicial 0.
     *  - Fecha actual (Date()).
     *  - El userName (o cadena vacía por defecto).
     */
    companion object {
        fun createDefault(userName: String = ""): Score {
            return Score(0, Date(), userName)
        }
    }
}
