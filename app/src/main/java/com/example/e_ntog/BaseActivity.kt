package com.example.e_ntog

import android.view.View
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    protected fun setupBackButton() {
        // Cari ID btn_back (ImageButton) atau fabNext (FAB)
        val ids = arrayOf(R.id.btn_back)

        for (id in ids) {
            val view = findViewById<View>(id)
            view?.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}