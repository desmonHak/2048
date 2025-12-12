package com.example.a2048.Views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a2048.Models.Score
import com.example.a2048.R
import com.example.a2048.Utils.DB_Score
import com.example.a2048.Utils.Difficulty
import com.example.a2048.Views.SettingGameView

/**
 * Activity principal del juego 2048.
 *
 * Se encarga de:
 * - Mostrar el tablero mediante [GameView].
 * - Gestionar el temporizador de la partida.
 * - Controlar la puntuación y mostrarla en la interfaz.
 * - Navegar a la pantalla de configuración ([SettingGameView]) y al registro de puntuaciones.
 */
class MainActivity : AppCompatActivity() {

    /** Vista personalizada que representa el tablero del juego. */
    private lateinit var gameView: GameView;

    /** para acceder a la base de datos de puntuaciones. */
    private lateinit var dbScore: DB_Score

    /** Barra de progreso que muestra el avance del temporizador. */
    @SuppressLint("ClickableViewAccessibility")
    private lateinit var progressBar: ProgressBar

    /** Texto que muestra la puntuación actual. */
    private lateinit var scoreText: TextView

    /** Botón para reiniciar la partida. */
    private lateinit var resetButton: Button

    /** Botón para abrir la pantalla de configuración de partida. */
    private lateinit var confButton: Button

    /** Duración total del temporizador de la partida, en milisegundos. */
    private var totalMillis: Long = 600_000L

    /** Tiempo de espera tras perder/ganar antes de resetear, en milisegundos. */
    private var timeSleep: Long = 2000L

    /** Referencia al temporizador de cuenta atrás de la partida. */
    private var timer: CountDownTimer? = null

    /** Indica si el jugador ha conseguido ya una victoria en la partida actual. */
    private var victoria: Boolean = false

    /**
     * Launcher para abrir [SettingGameView] y recibir el resultado de configuración.
     *
     * Al recibir `RESULT_OK`:
     * - Actualiza [totalMillis] con `"DURACION_JUEGO"`.
     * - Actualiza [gameView.boardSize] con `"SIZE_BOARD_ACTUAL"`.
     * - Convierte `"DIFICULTAD"` a [Difficulty] y la asigna a [gameView.dificultad].
     * - Reinicia el temporizador mediante [startTimer].
     * - Resetea el juego con [gameView.resetGame].
     */
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                // Recibir datos de Settings
                totalMillis = (data.getStringExtra("DURACION_JUEGO") ?: "600000").toLong()

                gameView.boardSize = (data.getStringExtra("SIZE_BOARD_ACTUAL") ?: "4").toInt()

                val difStr = data.getStringExtra("DIFICULTAD") ?: Difficulty.NORMAL.name
                val selectedDifficulty = try {
                    Difficulty.valueOf(difStr) // convierte "EASY"/"NORMAL"/"HARD" en enum
                } catch (e: IllegalArgumentException) {
                    Difficulty.NORMAL
                }

                gameView.dificultad = selectedDifficulty
                startTimer()  // Reiniciar timer
                gameView.resetGame()  // Aplicar dificultad
            }
        }
    }

    /*private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                // Recibir datos de Settings
                totalMillis = (data.getStringExtra("DURACION_JUEGO") ?: "600000").toLong()
                difficulty  = (data.getStringExtra("DIFICULTAD") ?: "1").toFloat()

                startTimer()  // Reiniciar timer
                gameView.resetGame()  // Aplicar dificultad
            }
        }
    }*/


    /**
     * Inicia o reinicia el temporizador de la partida.
     *
     * - Cancela el temporizador anterior si existe.
     * - Crea un nuevo [CountDownTimer] con duración [totalMillis].
     * - En cada tick, actualiza [progressBar] en función del tiempo transcurrido.
     * - Al finalizar, fija la barra al 100% de progreso.
     */
    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsed = totalMillis - millisUntilFinished
                val progress = ((elapsed.toDouble() / totalMillis) * 100).toInt()
                progressBar.progress = progress
            }
            override fun onFinish() {
                progressBar.progress = 100
            }
        }.start()
    }

    /**
     * Obtiene un nombre legible del dispositivo actual combinando fabricante y modelo.
     *
     * Ejemplos:
     * - `"samsung"` + `"SM-G950F"` → `"Samsung SM-G950F"`.
     * - `"Google"` + `"Pixel 7"` → `"Google Pixel 7"`.
     *
     * @return Nombre del dispositivo con palabras capitalizadas.
     */
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.capitalizeWords()
        } else {
            "${manufacturer.capitalizeWords()} $model"
        }
    }

    /**
     * Extensión para capitalizar cada palabra de un [String].
     *
     * Separa el texto por espacios y aplica [String.capitalize] a cada trozo.
     *
     * @receiver Cadena original.
     * @return Cadena con cada palabra capitalizada.
     */
    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.capitalize() }
    }

    /**
     * Metodo de ciclo de vida principal de la Activity.
     *
     * Tareas realizadas:
     * - Configura los insets para respetar barras del sistema.
     * - Inicializa vistas: [gameView], [progressBar], [scoreText], [resetButton],
     *   [confButton] y el botón de registro de puntuaciones.
     * - Configura callbacks de [gameView] para cambios de puntuación, victoria y derrota.
     * - Configura los botones de reset y configuración.
     * - Inicia una nueva partida y arranca el temporizador con [startTimer].
     *
     * @param savedInstanceState estado previamente guardado de la Activity, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        progressBar = findViewById(R.id.progressBar)
        scoreText = findViewById(R.id.scoreText)
        resetButton = findViewById<Button>(R.id.resetButton)
        confButton = findViewById<Button>(R.id.confButton)
        gameView = findViewById(R.id.gameView)
        gameView.boardSize = 4
        gameView.onScoreChange = { old, new ->
            scoreText.text = "Puntuación: ${new}"
        }
        val scoresButton = findViewById<Button>(R.id.registroButton)
        scoresButton.setOnClickListener {
            startActivity(Intent(this, ScoresActivity::class.java))
        }

        // Acción al perder la partida
        gameView.onActionLoser = { context ->
            Toast.makeText(context, "Has perdido calamar, reseteando", Toast.LENGTH_LONG).show()
            val handler = Handler(Looper.getMainLooper())

            val newScore = Score(
                points = gameView.score,
                userName = "Perdio: " + getDeviceName()
            )
            dbScore = DB_Score(this)
            dbScore.addScore(newScore)

            handler.postDelayed({
                gameView.resetGame()
            }, timeSleep) // despues de un segundo resetear el juego
            victoria = false
        }

        // Acción al ganar la partida
        gameView.onActionWin = { context ->
            if (!victoria) {
                Toast.makeText(context, "¡Has ganado piruleta!", Toast.LENGTH_LONG).show()
                val handler = Handler(Looper.getMainLooper())

                val newScore = Score(
                    points = gameView.score,
                    userName = "Gano: " + getDeviceName()
                )

                // ahora las puntuaciones en victoria se guardan al reiniciar el juego
                // asi el usuario puede seguir jugando!
                // dbScore = DB_Score(this)
                // dbScore.addScore(newScore)

                // handler.postDelayed({
                //    gameView.resetGame()
                // }, timeSleep) // despues de un segundo resetear el juego
                victoria = true
            }
        }

        // Ejemplo de tablero preconfigurado
        val board = arrayOf(
            intArrayOf(2, 4, 8, 16, 32, 64),
            intArrayOf(128, 256, 512, 1024, 1024, 0),
            intArrayOf(2, 4, 2, 4, 0, 0),
            intArrayOf(2, 4, 2, 4, 0, 0),
            intArrayOf(2, 4, 2, 4, 0, 0),
            intArrayOf(4, 2, 4, 0, 0, 0)
        )

        /**
         * Si dan al boton de reset, guardamos los datos
         * en la base de datos e indcamos que perdio.
         * indicamos si ganó o perdió según [victoria].
         */
        resetButton.setOnClickListener {
            val newScore = Score()
            newScore.points = gameView.score
            newScore.userName = if (victoria) {
                "Gano: " + getDeviceName()
            } else {
                "Perdio: " + getDeviceName()
            }

            dbScore = DB_Score(this)
            dbScore.addScore(newScore)
            gameView.resetGame()

            // resetear la variable victoria
            victoria = false
        }

        confButton.setOnClickListener {
            val intent = Intent(this, SettingGameView::class.java).apply {
                // Pasar valores actuales de GameView
                putExtra("DURACION_ACTUAL", totalMillis.toString())
                putExtra("DIFICULTAD_ACTUAL", gameView.dificultad.name)
                putExtra("TIMESLEEP_ACTUAL", timeSleep.toString())
                putExtra("SIZE_BOARD_ACTUAL", gameView.board.size.toString())
                putExtra("PUNTUACION_ACTUAL", gameView.score) // Asumiendo que GameView tiene 'score'
            }
            settingsLauncher.launch(intent) // O startActivity(intent)
        }

        gameView.resetGame()
        //gameView.setBoard(board)
        startTimer();

    }



}