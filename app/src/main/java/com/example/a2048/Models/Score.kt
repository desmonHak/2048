package com.example.a2048.Models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Score(
    val points: Int,
    val date: Date = Date(),
    val userName: String = ""
) {

    // Formato
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    override fun toString(): String {
        return "Score(points=$points, date=${getFormattedDate()}, user=$userName)"
    }

    // Metodo para crear Score vacío (por defecto)
    companion object {
        fun createDefault(userName: String = ""): Score {
            return Score(0, Date(), userName)
        }
    }
}
