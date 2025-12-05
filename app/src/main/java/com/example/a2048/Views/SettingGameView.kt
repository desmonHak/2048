package com.example.a2048.Views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.a2048.R

class SettingGameView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Mostrar valores actuales recibidos de MainActivity
        val durationEdit = findViewById<EditText>(R.id.editTextDurationTime)
        val difficultyEdit = findViewById<EditText>(R.id.editTextDificultad)

        durationEdit.setText(intent.getStringExtra("DURACION_ACTUAL") ?: "600000")
        difficultyEdit.setText(intent.getStringExtra("DIFICULTAD_ACTUAL") ?: "1")

        findViewById<Button>(R.id.backToMainButton).setOnClickListener {
            finish() // Volver sin guardar
        }

        findViewById<Button>(R.id.guadarButton).setOnClickListener { // Fix: guardarButton
            val resultIntent = Intent().apply {
                putExtra("DURACION_JUEGO", durationEdit.text.toString())
                putExtra("DIFICULTAD", difficultyEdit.text.toString())
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

    }
}