package com.example.e_ntog

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

open class BaseActivity : AppCompatActivity() {

    /**
     * Pasang FAB back (fabNext dari layout_arrow) DAN ImageView back arrow jika ada.
     * Harus dipanggil SETELAH setContentView().
     */
    protected fun setupBackButton() {
        // 1. FAB dari layout_arrow (fabNext)
        val fabBack = findViewById<FloatingActionButton>(R.id.fabNext)
        fabBack?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 2. ImageView back arrow (iv_back_arrow) — banyak layout pakai ini
        val ivBack = try {
            findViewById<android.widget.ImageView>(R.id.iv_back_arrow)
        } catch (e: Exception) { null }
        ivBack?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}
