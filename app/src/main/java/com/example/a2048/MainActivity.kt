package com.example.a2048

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var progressBar: ProgressBar
    private val totalTime = 10_000L // 10 segundos
    private val interval = 100L     // Actualiza cada 100 ms



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
            /*
            intArrayOf(2, 4, 8, 16, 32, 64),
            intArrayOf(128, 256, 512, 1024, 1024, 0),
            intArrayOf(2, 4, 2, 4, 0, 0),
            intArrayOf(2, 4, 2, 4, 0, 0),
            intArrayOf(2, 4, 2, 4, 0, 0),
            intArrayOf(4, 2, 4, 0, 0, 0)*/
            intArrayOf(0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0),
            intArrayOf(2, 0, 0, 0, 0, 0)
        )

        gameView.setBoard(board)

        progressBar = findViewById(R.id.progressBar)

        val progressShape = ShapeDrawable(RectShape()).apply {
            intrinsicHeight = 40   // grosor real de la barra
        }
        val progressClip = ClipDrawable(progressShape, Gravity.LEFT, ClipDrawable.HORIZONTAL)
        val background = ShapeDrawable(RectShape()).apply {
            intrinsicHeight = 40
            paint.color = Color.LTGRAY
        }
        val layers = arrayOf<Drawable>(background, progressClip)
        val layerDrawable = LayerDrawable(layers).apply {
            setId(0, android.R.id.background)
            setId(1, android.R.id.progress)
        }

        val timer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((totalTime - millisUntilFinished) * 900000 / totalTime).toInt()
                progressBar.progress = progress
            }

            override fun onFinish() {
                progressBar.progress = 100
                Toast.makeText(this@MainActivity, "¡Tu Tiempo Perruno Terminó!", Toast.LENGTH_SHORT).show()
            }
        }

        timer.start()

    }

    class MainActivity : AppCompatActivity() {

        lateinit var scoreText: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            //scoreText = findViewById(R.id.scoreText)
        }

        fun updateScoreDisplay(score: Int) {
            scoreText.text = "Score: $score"
        }
    }
}







