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

    companion object {
        fun createDefault(userName: String = ""): Score {
            return Score(0, Date(), userName)
        }
    }
}
