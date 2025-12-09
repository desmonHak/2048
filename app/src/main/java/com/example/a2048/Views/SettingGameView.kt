package com.example.a2048.Views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.a2048.R
import com.example.a2048.Utils.Difficulty

class SettingGameView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnEasy = findViewById<Button>(R.id.easy)
        val btnNormal = findViewById<Button>(R.id.normal)
        val btnHard = findViewById<Button>(R.id.hard)



        // Mostrar valores actuales recibidos de MainActivity
        val durationEdit = findViewById<EditText>(R.id.editTextDurationTime)
        //val difficultyEdit = findViewById<EditText>(R.id.editTextDificultad)

        btnEasy.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("DIFICULTAD", Difficulty.EASY.name)
                putExtra("DURACION_JUEGO", intent.getStringExtra("DURACION_ACTUAL") ?: Difficulty.NORMAL.name/*"600000"*/)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        btnNormal.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("DIFICULTAD", Difficulty.NORMAL.name)
                putExtra("DURACION_JUEGO", intent.getStringExtra("DURACION_ACTUAL") ?: "600000")
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        btnHard.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("DIFICULTAD", Difficulty.HARD.name)
                putExtra("DURACION_JUEGO", intent.getStringExtra("DURACION_ACTUAL") ?: "600000")
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }



        durationEdit.setText(intent.getStringExtra("DURACION_ACTUAL") ?: "600000")
        //difficultyEdit.setText(intent.getStringExtra("DIFICULTAD_ACTUAL") ?: "1")

        findViewById<Button>(R.id.backToMainButton).setOnClickListener {
            finish() // Volver sin guardar
        }

        findViewById<Button>(R.id.guadarButton).setOnClickListener { // Fix: guardarButton
            val resultIntent = Intent().apply {
                putExtra("DURACION_JUEGO", durationEdit.text.toString())
               // putExtra("DIFICULTAD", difficultyEdit.text.toString())
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

    }
}