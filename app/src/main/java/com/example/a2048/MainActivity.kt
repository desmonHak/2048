package com.example.a2048

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set


class MainActivity : AppCompatActivity() {
    private lateinit var tempBitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val gameView: GameView = findViewById(R.id.gameView)

        val board = arrayOf(
            intArrayOf(2, 4, 2, 4, 0, 0),
            intArrayOf(4, 2, 4, 2, 0, 0),
            intArrayOf(2, 4, 2, 4, 0, 0),
            intArrayOf(2, 4, 2, 4, 0, 0),
            intArrayOf(2, 4, 2, 4, 0, 0),
            intArrayOf(4, 2, 4, 0, 0, 0)
        )

        gameView.setBoard(board)

    }


}