package com.example.a2048.Views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.a2048.Utils.Difficulty
import kotlin.properties.Delegates
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    // ---------- CONSTANTES ----------
    private val swipeThreshold = 100
    private var maxTime = 10000L // 10 segundos


    // ---------- ESTADO DEL JUEGO ----------

    var boardSize = 4
        set(value) {
            field = value
            board = Array(value) { IntArray(value) }
            setBoard(board) // en este momento, boardSize sigue siendo 6, pero value es el
            // nuevo valor, no podemos hacer boardSize = value por que formara un set recursivo
            // creando un bucle
            invalidate()
        }

    var board = Array(boardSize) { IntArray(boardSize) }
        get() = field
        set(value) {
            field = value
            invalidate()
        }

    /**
     * onScoreChange almacena el lambda que usara los valores para
     * algun fin, como mostrarlo en la pantalla. este metodo es
     * llamado por score el cual permite observar cambios en la variable
     * si la puntuacion se actualiza, el metodo almacenado en
     * onScoreChange sera invocado, reciviendo el nueva y el antiguo valor
     */
    var onScoreChange: (Int, Int) -> Unit = { _, _ -> }

    /**
     * que hacer al ganar
     */
    var onActionWin: (Context) -> Unit = { _ -> }

    /**
     * que hacer al perder
     */
    var onActionLoser: (Context) -> Unit = { _ -> }

    var score: Int by Delegates.observable(0) { _, old, new ->
        onScoreChange(old, new)
    }
    private var moveCounter = 0
    private var timeLeft = maxTime
    private var lastUpdateTime = System.currentTimeMillis()

    /**
     * Nivel de dificultad del juego.
     *
     * Rango: `0.0f` (muy fácil) a `1.0f` (muy difícil)
     * - **0.0f**: 90% tiles = 2, 10% tiles = 4
     * - **0.5f**: 75% tiles = 2, 25% tiles = 4 (valor por defecto)
     * - **1.0f**: 60% tiles = 2, 40% tiles = 4
     *
     * @see addRandomTile
     */
    var dificultad: Difficulty = Difficulty.NORMAL // Valor por defecto medio
    /*var difficulty: Float = 0.5f // Valor por defecto medio
        /**
         * Setter que limita el valor entre 0.0f y 1.0f.
         *
         * @param value Nuevo valor de dificultad
         */
        set(value) {
            field = value.coerceIn(0.0f, 1.0f) // Limitar entre 0 y 1
        }*/

    // ---------- GESTOS ----------
    private var startX = 0f
    private var startY = 0f

    // ---------- PINTURAS ----------
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER; textSize = 80f
    }
    private val emptyTilePaint = Paint().apply { color = Color.LTGRAY }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK; textAlign = Paint.Align.CENTER; textSize = 70f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                val dx = event.x - startX
                val dy = event.y - startY

                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > swipeThreshold) swipeRight()
                    else if (dx < -swipeThreshold) swipeLeft()
                } else {
                    if (dy > swipeThreshold) swipeDown()
                    else if (dy < -swipeThreshold) swipeUp()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        updateTimer()
        drawBoard(canvas)
    }

    private fun drawBoard(canvas: Canvas) {
        val tileSize = width / board.size

        for (r in 0 until board.size) {
            for (c in 0 until board.size) {
                val x = c * tileSize
                val y = r * tileSize

                // Fondo vacío
                canvas.drawRect(
                    x.toFloat(), y.toFloat(),
                    (x + tileSize).toFloat(), (y + tileSize).toFloat(),
                    emptyTilePaint
                )

                val value = board[r][c]
                if (value != 0) {
                    tilePaint.color = tileColor(value)
                    canvas.drawRect(
                        x.toFloat(), y.toFloat(),
                        (x + tileSize).toFloat(), (y + tileSize).toFloat(),
                        tilePaint
                    )
                    canvas.drawText(
                        value.toString(),
                        x + tileSize / 2f,
                        y + tileSize / 2f + 30f,
                        textPaint
                    )
                }
            }
        }
    }

    //función para la dificultad
    fun spawnTile() {
        var dificultad: Difficulty = Difficulty.NORMAL // Valor por defecto medio
        val prob = Math.random()
        val value = if (prob < dificultad.probabilidad) 4 else 2
        // coloca la ficha en una celda vacía
    }

    private fun tileColor(value: Int) = when (value) {
        2 -> Color.rgb(253, 185, 223)
        4 -> Color.rgb(252, 141, 202)
        8 -> Color.rgb(251, 100, 182)
        16 -> Color.rgb(250, 51, 160)
        32 -> Color.rgb(230, 24, 118)
        64 -> Color.rgb(216, 249, 153)
        128 -> Color.rgb(154, 230, 48)
        256 -> Color.rgb(178, 243, 57)
        512 -> Color.rgb(162, 241, 14)
        1024 -> Color.rgb(98, 79, 222)
        2048 -> Color.rgb(133, 118, 229)
        else -> Color.GRAY
    }

    // ---------- LÓGICA DEL JUEGO ----------
    fun resetGame() {
        // Primero limpiar el board actual con su tamaño
        for (r in board.indices) {
            for (c in board[r].indices) {
                board[r][c] = 0
            }
        }

        score = 0
        moveCounter = 0
        timeLeft = maxTime
        lastUpdateTime = System.currentTimeMillis()
        addRandomTile()
        addRandomTile()
        invalidate()
    }

    /**
     * Genera un nuevo tile aleatorio en una celda vacía del tablero.
     *
     * **Lógica de dificultad:**
     * - Calcula probabilidad de generar `4` vs `2` basada en [difficulty]
     * - Rango de probabilidad de `4`: 10% (fácil) → 40% (difícil)
     * - Usa `Random.nextDouble()` para decisión probabilística
     *
     * **Algoritmo:**
     * 1. Encuentra todas las celdas vacías (`board[r][c] == 0`)
     * 2. Selecciona una celda aleatoria entre las vacías
     * 3. Aplica fórmula de dificultad: `fourProbability = 0.1f + (difficulty * 0.3f)`
     * 4. Coloca tile `4` o `2` según probabilidad
     *
     * @see difficulty
     */
    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until board.size) {
            for (c in 0 until board.size) {
                if (board[r][c] == 0) emptyCells.add(Pair(r, c))
            }
        }

        if (emptyCells.isNotEmpty()) {
            val (row, col) = emptyCells.random()

            // Usa la dificultad seleccionada
            val fourProbability = when (dificultad) {
                Difficulty.EASY -> 0.1f   // 10% de 4
                Difficulty.NORMAL -> 0.3f // 30% de 4
                Difficulty.HARD -> 0.5f   // 50% de 4
            }
            Log.d("GameView", "Dificultad actual: ${dificultad.name}, probabilidad de 4: $fourProbability")
            board[row][col] = if (Random.Default.nextDouble() < fourProbability) 4 else 2
        }
    }

    private fun checkWin() = board.any { row -> row.any { it == 2048 } }
    private fun checkLose(): Boolean {
        if (board.any { row -> row.any { it == 0 } }) return false
        for (r in 0 until board.size) for (c in 0 until board.size - 1) {
            if (board[r][c] == board[r][c + 1]) return false
        }
        for (c in 0 until board.size) for (r in 0 until board.size - 1) {
            if (board[r][c] == board[r + 1][c]) return false
        }
        return true
    }

    private fun moveBoard(newBoard: Array<IntArray>) {
        try {
            var moved = false
            for (r in 0 until board.size) {
                if (!board[r].contentEquals(newBoard[r])) moved = true
            }
            for (r in 0 until board.size) board[r] = newBoard[r].copyOf()

            if (moved) {
                addRandomTile()
                invalidate()
                if (checkWin()) {
                    onActionWin.invoke(context)
                } else if (checkLose()) {
                    onActionLoser.invoke(context)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("GameView", "Error en moveBoard", e)
        }
    }

    // ---------- MOVIMIENTOS ----------
    private fun swipeLeft() {
        moveCounter++
        resetTimer()
        val newBoard = Array(board.size) { IntArray(board.size) }
        for (r in 0 until board.size) {
            val line = board[r].filter { it != 0 }.toMutableList()
            var i = 0
            while (i < line.size - 1) {
                if (line[i] == line[i + 1]) {
                    line[i] *= 2
                    score += line[i]
                    line.removeAt(i + 1)
                }
                i++
            }
            while (line.size < board.size) line.add(0)
            newBoard[r] = line.toIntArray()
        }
        moveBoard(newBoard)
    }

    private fun swipeRight() {
        moveCounter++
        resetTimer()
        val newBoard = Array(board.size) { IntArray(board.size) }
        for (r in 0 until board.size) {
            val line = board[r].filter { it != 0 }.toMutableList()
            var i = line.size - 1
            while (i > 0) {
                if (line[i] == line[i - 1]) {
                    line[i] *= 2
                    score += line[i]
                    line.removeAt(i - 1)
                    i--
                }
                i--
            }
            while (line.size < board.size) line.add(0, 0)
            newBoard[r] = line.toIntArray()
        }
        moveBoard(newBoard)
    }

    private fun swipeUp() {
        moveCounter++
        resetTimer()
        val newBoard = Array(board.size) { IntArray(board.size) }
        for (c in 0 until board.size) {
            val col = MutableList(board.size) { board[it][c] }.filter { it != 0 }.toMutableList()
            var i = 0
            while (i < col.size - 1) {
                if (col[i] == col[i + 1]) {
                    col[i] *= 2
                    score += col[i]
                    col.removeAt(i + 1)
                }
                i++
            }
            while (col.size < board.size) col.add(0)
            for (r in 0 until board.size) newBoard[r][c] = col[r]
        }
        moveBoard(newBoard)
    }

    private fun swipeDown() {
        moveCounter++
        resetTimer()
        val newBoard = Array(board.size) { IntArray(board.size) }
        for (c in 0 until board.size) {
            val col = MutableList(board.size) { board[it][c] }.filter { it != 0 }.toMutableList()
            var i = col.size - 1
            while (i > 0) {
                if (col[i] == col[i - 1]) {
                    col[i] *= 2
                    score += col[i]
                    col.removeAt(i - 1)
                    i--
                }
                i--
            }
            while (col.size < board.size) col.add(0, 0)
            for (r in 0 until board.size) newBoard[r][c] = col[r]
        }
        moveBoard(newBoard)
    }

    // ---------- TIMER ----------
    private fun updateTimer() {
        val now = System.currentTimeMillis()
        val delta = now - lastUpdateTime
        lastUpdateTime = now
        timeLeft -= delta
        if (timeLeft < 0) timeLeft = 0
    }

    private fun resetTimer() {
        timeLeft = maxTime
        lastUpdateTime = System.currentTimeMillis()
    }

    /**
     * Al tener un atributo llamado board, y definir el metodo set de la forma tradicional.
     * El nombre del metodo set generado por kotlin sera setBoard. Al tener nosotros otro
     * metodo setBoard, en este caso para actualizar el atributo board con otro tablero, debemos
     * hacer que kotlin genere otro nombre interno para esta funcion, para esto usamos JvmName, lo
     * cual nos permite definir a nivel de bytecode el nombre de la funcion, en este caso, esta
     * funcion setBoard al final a nivel de bytecode recibira el nombre de updateBoardJvm.
     *
     * Esto es la solucion propuesta al problema "Platform declaration clash":
     *  - https://stackoverflow.com/questions/59920597/kotlin-platform-declaration-clash
     */
    @JvmName("updateBoardJvm")
    fun setBoard(newBoard: Array<IntArray>) {
        val size = newBoard.size  // Usa el tamaño del nuevo board
        for (r in 0 until size) {
            for (c in 0 until size) {
                board[r][c] = newBoard[r][c]
            }
        }
        invalidate()
    }
}