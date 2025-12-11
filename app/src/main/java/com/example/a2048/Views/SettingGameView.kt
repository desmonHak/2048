package com.example.a2048.Views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.a2048.R
import com.example.a2048.Utils.Difficulty

class SettingGameView : AppCompatActivity() {

    lateinit var durationEdit: EditText
    lateinit var editTextSizeBoard: EditText

    var dificultad: Difficulty = Difficulty.NORMAL

    fun add_conf_to_intent(intent: Intent): Intent {
        return intent.apply {
            putExtra("DIFICULTAD", dificultad.name)

            // Validar duración: mínimo 4 dígitos y > 0
            val durationText = durationEdit.text.toString()
            if (durationText.length >= 4 && durationText.toIntOrNull() != null && durationText.toInt() > 0) {
                putExtra("DURACION_JUEGO", durationText)
            }

            // Validar tamaño tablero: número válido > 0
            val sizeText = editTextSizeBoard.text.toString()
            val size = sizeText.toIntOrNull()
            if (size != null && size > 0) {
                putExtra("SIZE_BOARD_ACTUAL", size.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Inicializar vistas
        durationEdit = findViewById(R.id.editTextDurationTime)
        editTextSizeBoard = findViewById(R.id.editTextSizeBoard)

        // Cargar valores actuales desde intent(desde el MainActivity)
        durationEdit.setText(intent.getStringExtra("DURACION_ACTUAL") ?: "600000")
        editTextSizeBoard.setText(intent.getStringExtra("SIZE_BOARD_ACTUAL") ?: "4")
        var dificultadStr: String = (intent.getStringExtra("DIFICULTAD_ACTUAL") ?: Difficulty.NORMAL.name)
        dificultad = try {
            Difficulty.valueOf(dificultadStr) // convierte "EASY"/"NORMAL"/"HARD" en enum
        } catch (e: IllegalArgumentException) {
            Difficulty.NORMAL
        }

        // Botones dificultad
        findViewById<Button>(R.id.easy).setOnClickListener {
            dificultad = Difficulty.EASY
            val resultIntent = Intent().apply { add_conf_to_intent(this) }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        findViewById<Button>(R.id.normal).setOnClickListener {
            dificultad = Difficulty.NORMAL
            val resultIntent = Intent().apply { add_conf_to_intent(this) }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        findViewById<Button>(R.id.hard).setOnClickListener {
            dificultad = Difficulty.HARD
            val resultIntent = Intent().apply { add_conf_to_intent(this) }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // Botón volver sin guardar
        findViewById<Button>(R.id.backToMainButton).setOnClickListener {
            finish()
        }

        // Botón guardar
        findViewById<Button>(R.id.guadarButton).setOnClickListener {
            val resultIntent = Intent().apply { add_conf_to_intent(this) }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
