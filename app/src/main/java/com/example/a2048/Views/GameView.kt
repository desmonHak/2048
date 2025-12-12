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

    /**
     * Tamaño del tablero (n x n).
     *
     * Al cambiar este valor:
     * - Se crea un nuevo tablero con el nuevo tamaño.
     * - Se reasigna internamente el atributo [board].
     * - Se fuerza un redibujado mediante [invalidate].
     */
    var boardSize = 4
        set(value) {
            field = value
            board = Array(value) { IntArray(value) }
            setBoard(board) // en este momento, boardSize sigue siendo 6, pero value es el
            // nuevo valor, no podemos hacer boardSize = value por que formara un set recursivo
            // creando un bucle
            invalidate()
        }

    /**
     * Matriz que representa el tablero del juego.
     *
     * Cada celda contiene:
     * - `0` para celdas vacías.
     * - Potencias de 2 para fichas activas (2, 4, 8, ..., 2048).
     *
     * Cualquier asignación al tablero fuerza un redibujado de la vista.
     */
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


    /**
     * Puntuación actual del jugador.
     *
     * Es una propiedad observable: cada vez que cambia su valor, se invoca
     * el callback definido en [onScoreChange].[web:12][web:18]
     */
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
     * Esta dificultad se aplica en [addRandomTile].
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

    /**
     * Gestiona los eventos táctiles para detectar gestos de deslizamiento.
     *
     * Lógica:
     * - En `ACTION_DOWN` almacena la posición inicial del toque.
     * - En `ACTION_UP` calcula desplazamiento en X/Y y determina la dirección del swipe.
     * - Llama a [swipeLeft], [swipeRight], [swipeUp] o [swipeDown] según corresponda.
     *
     * @return `true` si el evento ha sido manejado por la vista.
     */
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

    /**
     * Metodo de dibujo principal de la vista.
     *
     * En cada frame:
     * - Actualiza el temporizador con [updateTimer].
     * - Dibuja el tablero y las fichas con [drawBoard].
     *
     * @param canvas lienzo de dibujo proporcionado por el sistema.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        updateTimer()
        drawBoard(canvas)
    }

    /**
     * Dibuja el tablero y todas las fichas en el [Canvas].
     *
     * Lógica:
     * - Divide el ancho de la vista por el tamaño del tablero para obtener el tamaño de cada celda.
     * - Dibuja el fondo de cada celda.
     * - Dibuja el rectángulo de la ficha y su valor si la celda es distinta de 0.
     *
     * @param canvas lienzo sobre el que se dibuja el tablero.
     */
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

    /**
     * Devuelve el color asociado a una ficha según su valor.
     *
     * @param value valor de la ficha (2, 4, 8, ..., 2048).
     * @return color ARGB que se usará para pintar el fondo de la ficha.
     */
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
    /**
     * Reinicia completamente la partida.
     *
     * Acciones:
     * - Limpia el tablero (todas las celdas a 0).
     * - Reinicia puntuación, contador de movimientos y temporizador.
     * - Añade dos fichas aleatorias iniciales mediante [addRandomTile].
     * - Llama a [invalidate] para redibujar.
     */
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
     * Genera una nueva ficha aleatoria en una celda vacía del tablero.
     *
     * Usa la dificultad actual [dificultad] para decidir la probabilidad
     * de generar un `4` frente a un `2`:
     * - EASY: 10% de `4`.
     * - NORMAL: 30% de `4`.
     * - HARD: 50% de `4`.
     *
     * Si no hay celdas vacías, no hace nada.
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

    /**
     * Comprueba si el jugador ha ganado la partida.
     *
     * La condición de victoria es tener al menos una ficha con valor 2048.
     *
     * @return `true` si existe una ficha 2048 en el tablero, `false` en caso contrario.
     */
    private fun checkWin() = board.any { row -> row.any { it == 2048 } }

    /**
     * Comprueba si el jugador ha perdido la partida.
     *
     * Condiciones de derrota:
     * - No hay celdas vacías.
     * - No hay movimientos posibles (no hay fichas adyacentes iguales).
     *
     * @return `true` si no hay movimientos disponibles, `false` si aún se puede jugar.
     */
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


    /**
     * Actualiza el tablero con un nuevo estado y gestiona eventos de juego.
     *
     * Lógica:
     * - Comprueba si alguna fila ha cambiado respecto a [board].
     * - Si ha habido movimiento:
     *   - Genera una nueva ficha con [addRandomTile].
     *   - Redibuja la vista con [invalidate].
     *   - Comprueba victoria ([checkWin]) o derrota ([checkLose]) y ejecuta
     *     [onActionWin] o [onActionLoser] según corresponda.
     *
     * En caso de error, muestra un [Toast] y registra el error en el log.
     *
     * @param newBoard nuevo estado del tablero tras un movimiento.
     */
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
    /**
     * Aplica un movimiento hacia la izquierda a todo el tablero.
     *
     * Para cada fila:
     * - Comprime las fichas eliminando ceros.
     * - Fusiona fichas adyacentes iguales sumando su valor y aumentando [score].
     * - Rellena con ceros al final hasta recuperar el tamaño de la fila.
     *
     * Finalmente delega en [moveBoard] para aplicar el nuevo estado.
     */
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

    /**
     * Aplica un movimiento hacia la derecha a todo el tablero.
     *
     * Similar a [swipeLeft], pero:
     * - Recorre la fila desde el final.
     * - Inserta ceros al principio de la lista.
     */
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

    /**
     * Aplica un movimiento hacia arriba a todo el tablero.
     *
     * Para cada columna:
     * - Construye una lista con los valores no nulos.
     * - Fusiona fichas adyacentes iguales.
     * - Rellena con ceros al final.
     * - Escribe el resultado de vuelta en la matriz [board].
     */
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

    /**
     * Aplica un movimiento hacia abajo a todo el tablero.
     *
     * Similar a [swipeUp], pero:
     * - Recorre la columna desde el final.
     * - Inserta ceros al principio de la lista.
     */
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
    /**
     * Actualiza el temporizador de la partida.
     *
     * Calcula el tiempo transcurrido desde la última actualización y lo
     * descuenta de [timeLeft]. Si el resultado es negativo, lo fija a 0.
     */
    private fun updateTimer() {
        val now = System.currentTimeMillis()
        val delta = now - lastUpdateTime
        lastUpdateTime = now
        timeLeft -= delta
        if (timeLeft < 0) timeLeft = 0
    }

    /**
     * Reinicia el temporizador al valor máximo configurado en [maxTime].
     *
     * También actualiza [lastUpdateTime] con el tiempo actual del sistema.
     */
    private fun resetTimer() {
        timeLeft = maxTime
        lastUpdateTime = System.currentTimeMillis()
    }

    /**
     * Actualiza el tablero con un nuevo array 2D sin colisionar con el `setter` generado.
     *
     * Debido a que existe una propiedad [board], el compilador generaría internamente
     * un metodo `setBoard`. Para evitar el choque de nombres a nivel de bytecode
     * (*Platform declaration clash*), se anota este metodo con [JvmName], dandole
     * el nombre interno `updateBoardJvm`.[web:16][web:19]
     *
     * @param newBoard nuevo estado completo del tablero.
     *
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