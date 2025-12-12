package com.example.a2048.Views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.a2048.R
import com.example.a2048.Utils.Difficulty


/**
 * Pantalla de configuración de la partida de 2048.
 *
 * Permite al usuario:
 * - Seleccionar la dificultad del juego.
 * - Configurar la duración de la partida.
 * - Definir el tamaño del tablero.
 *
 * Los valores seleccionados se devuelven al `Activity` llamante mediante
 * un `Intent` con extras.
 */
class SettingGameView : AppCompatActivity() {

    /**
     * Campo de texto para introducir la duración de la partida en milisegundos.
     */
    lateinit var durationEdit: EditText

    /**
     * Campo de texto para introducir el tamaño del tablero (n x n).
     */
    lateinit var editTextSizeBoard: EditText

    /**
     * Dificultad seleccionada para la partida.
     *
     * Se inicializa con [Difficulty.NORMAL] y se actualiza al pulsar
     * los botones de dificultad.
     */
    var dificultad: Difficulty = Difficulty.NORMAL

    /**
     * Añade la configuración actual de la partida como extras al [Intent] proporcionado.
     *
     * Claves usadas:
     * - `"DIFICULTAD"`: nombre de la dificultad seleccionada ([Difficulty.name]).
     * - `"DURACION_JUEGO"`: duración en milisegundos, solo si es un número válido
     *   de al menos 4 dígitos y mayor que 0.
     * - `"SIZE_BOARD_ACTUAL"`: tamaño del tablero, solo si es un número válido > 0.
     *
     * @param intent Intent al que se le añadirán los parámetros de configuración.
     * @return El mismo [Intent] recibido, para permitir llamadas encadenadas.
     *
     * @see Difficulty
     */
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

    /**
     * Inicializa la pantalla de configuración.
     *
     * Tareas principales:
     * - Inicializa los campos de texto y los rellena con los valores actuales
     *   recibidos desde el `Intent` llamante (`DURACION_ACTUAL`, `SIZE_BOARD_ACTUAL`,
     *   `DIFICULTAD_ACTUAL`).
     * - Configura los listeners de los botones de dificultad y de guardado.
     *
     * Al pulsar un botón de dificultad o el de guardar:
     * - Se construye un `Intent` de resultado usando [add_conf_to_intent].
     * - Se devuelve el resultado con [setResult] y se cierra la Activity con [finish].
     *
     * @param savedInstanceState estado previamente guardado de la Activity, si existe.
     */
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
