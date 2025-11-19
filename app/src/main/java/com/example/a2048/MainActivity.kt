package com.example.a2048

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {

    private var left = 100f
    private var top = 100f
    private val width = 200f
    private val height = 200f

    private lateinit var imageView: ImageView
    private lateinit var tempBitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private var color_background: Int = Color.GRAY;
    lateinit private var arrs : Array<RectF>

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
        imageView = findViewById<ImageView>(R.id.imageView);
        imageView.setBackgroundColor(color_background)

        tempBitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        canvas = Canvas(tempBitmap)

        paint = Paint().apply {
            strokeWidth = 5F
            color = Color.RED
            style = Paint.Style.FILL_AND_STROKE
        }



        val rectF = RectF(left*2, top*2, left + width*2, top + height *2)
        val rectF1 = RectF(left, top, left + width, top + height )
        arrs = arrayOf(rectF, rectF1)

        canvas.drawRect(rectF, paint)
        paint.color = Color.CYAN;
        imageView.setOnTouchListener  { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                for (rec in arrs) {
                    // Actualizar la posición del rectángulo centrado en la posición del toque
                    left = event.x - rec.width() / 2
                    top = event.y - rec.height() / 2

                }
                drawRect()
            }
            true
        }
        canvas.drawRect(rectF1, paint)


        imageView.setImageBitmap(tempBitmap)


    }

    private fun drawRect() {
        // Limpiar el bitmap para no acumular dibujos
        canvas.drawColor(color_background)

        for (rec in arrs) {
            val rectF = RectF(left, top, left + width, top + height)
            canvas.drawRect(rec, paint)
        }

        imageView.setImageBitmap(tempBitmap)
    }
}