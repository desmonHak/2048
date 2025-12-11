package com.example.a2048.Utils

enum class Difficulty(val probabilidad: Double) {
    EASY(0.1),    // 10% probabilidad de que aparezca un 4
    NORMAL(0.3),  // 30% probabilidad de 4
    HARD(0.5);     // 50% probabilidad de 4

    companion object {
        fun fromStringOrNull(value: String?): Difficulty? = when (value?.lowercase()) {
            "easy", "facil", "fácil" -> EASY
            "normal" -> NORMAL
            "hard", "dificil", "difícil" -> HARD
            else -> null
        }
    }
}