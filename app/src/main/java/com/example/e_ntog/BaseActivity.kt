package com.example.e_ntog

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

open class BaseActivity : AppCompatActivity() {
    protected fun setupBackButton() {
        // Pastikan ID-nya fabNext sesuai XML layout_arrow lu
        val btnBack = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabNext)
        btnBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}