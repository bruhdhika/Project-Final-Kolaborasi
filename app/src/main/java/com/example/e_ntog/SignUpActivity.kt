package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Inisialisasi tombol daftar berdasarkan ID di XML
        val btnDaftar = findViewById<Button>(R.id.btn_daftar)

        btnDaftar.setOnClickListener {
            // Setelah daftar, arahkan ke SignInActivity untuk login
            val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
            startActivity(intent)
            // Selesaikan activity ini agar tidak menumpuk
            finish()
        }
    }
}