package com.example.a2048

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    // ---------- CONSTANTES Y TABLERO ----------
    private val boardSize = 6
    private val board = Array(boardSize) { IntArray(boardSize) }

    // Pinturas para dibujar tiles y texto
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = 80f }
    private val emptyTilePaint = Paint().apply { color = Color.LTGRAY }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textAlign = Paint.Align.CENTER; textSize = 100f }

    // ---------- GESTOS ----------
    private var startX = 0f
    private var startY = 0f
    private val swipeThreshold = 100

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
                    // Swipe horizontal
                    if (dx > swipeThreshold) swipeRight()
                    else if (dx < -swipeThreshold) swipeLeft()
                } else {
                    // Swipe vertical
                    if (dy > swipeThreshold) swipeDown()
                    else if (dy < -swipeThreshold) swipeUp()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // ---------- DIBUJAR TABLERO ----------
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val tileSize = width / boardSize

        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val x = c * tileSize
                val y = r * tileSize

                // Dibujar fondo del tile vacío
                canvas.drawRect(x.toFloat(), y.toFloat(), (x + tileSize).toFloat(), (y + tileSize).toFloat(), emptyTilePaint)

                val value = board[r][c]
                if (value != 0) {
                    // Dibujar tile con color según valor
                    tilePaint.color = tileColor(value)
                    canvas.drawRect(x.toFloat(), y.toFloat(), (x + tileSize).toFloat(), (y + tileSize).toFloat(), tilePaint)

                    // Dibujar número en el centro
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

    // ---------- COLORES DE CADA TILE ----------
    private fun tileColor(value: Int) = when (value) {
        2 -> Color.rgb(238, 228, 218)
        4 -> Color.rgb(237, 224, 200)
        8 -> Color.rgb(242, 177, 121)
        16 -> Color.rgb(245, 149, 99)
        32 -> Color.rgb(246, 124, 95)
        64 -> Color.rgb(246, 94, 59)
        128 -> Color.rgb(237, 207, 114)
        256 -> Color.rgb(237, 204, 97)
        512 -> Color.rgb(237, 200, 80)
        1024 -> Color.rgb(237, 197, 63)
        2048 -> Color.rgb(237, 194, 46)
        else -> Color.GRAY
    }

    // ---------- RESETEAR JUEGO ----------
    fun resetGame() {
        for (r in 0 until boardSize) for (c in 0 until boardSize) board[r][c] = 0
        addRandomTile()
        addRandomTile()
        invalidate()
    }

    // ---------- GENERACIÓN DE NUEVOS TILES ----------
    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()

        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                if (board[r][c] == 0) emptyCells.add(Pair(r, c))
            }
        }

        if (emptyCells.isNotEmpty()) {
            val (row, col) = emptyCells.random()
            board[row][col] = if (Random.nextDouble() < 0.9) 2 else 4
        }
    }

    // ---------- VERIFICAR VICTORIA ----------
    private fun checkWin() = board.any { row -> row.any { it == 2048 } }

    // ---------- VERIFICAR DERROTA ----------
    private fun checkLose(): Boolean {
        // Hay celda vacía
        if (board.any { row -> row.any { it == 0 } }) return false

        // Movimientos posibles horizontal
        for (r in 0 until boardSize) for (c in 0 until boardSize - 1) {
            if (board[r][c] == board[r][c + 1]) return false
        }

        // Movimientos posibles vertical
        for (c in 0 until boardSize) for (r in 0 until boardSize - 1) {
            if (board[r][c] == board[r + 1][c]) return false
        }

        return true
    }

    // ---------- FUNCIONES DE MOVIMIENTO ----------
    private fun moveBoard(newBoard: Array<IntArray>) {
        var moved = false

        for (r in 0 until boardSize) {
            if (!board[r].contentEquals(newBoard[r])) moved = true
        }

        for (r in 0 until boardSize) board[r] = newBoard[r].copyOf()

        if (moved) {
            addRandomTile()
            invalidate()
            if (checkWin()) {
                Log.i("Estado", "Victoria!")
                // TODO: mostrar mensaje de victoria
            } else if (checkLose()) {
                Log.i("Estado", "Derrota!")
                // TODO: mostrar mensaje de derrota
            }
        }
    }

    fun swipeLeft() {
        val newBoard = Array(boardSize) { IntArray(boardSize) }
        for (r in 0 until boardSize) {
            val line = board[r].filter { it != 0 }.toMutableList()
            var i = 0
            while (i < line.size - 1) {
                if (line[i] == line[i + 1]) {
                    line[i] *= 2
                    line.removeAt(i + 1)
                }
                i++
            }
            while (line.size < boardSize) line.add(0)
            newBoard[r] = line.toIntArray()
        }
        moveBoard(newBoard)
    }

    fun swipeRight() {
        val newBoard = Array(boardSize) { IntArray(boardSize) }
        for (r in 0 until boardSize) {
            val line = board[r].filter { it != 0 }.toMutableList()
            var i = line.size - 1
            while (i > 0) {
                if (line[i] == line[i - 1]) {
                    line[i] *= 2
                    line.removeAt(i - 1)
                    i--
                }
                i--
            }
            while (line.size < boardSize) line.add(0, 0)
            newBoard[r] = line.toIntArray()
        }
        moveBoard(newBoard)
    }

    fun swipeUp() {
        val newBoard = Array(boardSize) { IntArray(boardSize) }
        for (c in 0 until boardSize) {
            val col = MutableList(boardSize) { board[it][c] }.filter { it != 0 }.toMutableList()
            var i = 0
            while (i < col.size - 1) {
                if (col[i] == col[i + 1]) {
                    col[i] *= 2
                    col.removeAt(i + 1)
                }
                i++
            }
            while (col.size < boardSize) col.add(0)
            for (r in 0 until boardSize) newBoard[r][c] = col[r]
        }
        moveBoard(newBoard)
    }

    fun swipeDown() {
        val newBoard = Array(boardSize) { IntArray(boardSize) }
        for (c in 0 until boardSize) {
            val col = MutableList(boardSize) { board[it][c] }.filter { it != 0 }.toMutableList()
            var i = col.size - 1
            while (i > 0) {
                if (col[i] == col[i - 1]) {
                    col[i] *= 2
                    col.removeAt(i - 1)
                    i--
                }
                i--
            }
            while (col.size < boardSize) col.add(0, 0)
            for (r in 0 until boardSize) newBoard[r][c] = col[r]
        }
        moveBoard(newBoard)
    }

    // ---------- OPCIONAL: Establecer tablero desde Activity ----------
    fun setBoard(newBoard: Array<IntArray>) {
        for (r in 0 until boardSize) for (c in 0 until boardSize) board[r][c] = newBoard[r][c]
        invalidate()
    }
}
