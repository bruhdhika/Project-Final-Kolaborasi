package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)

        databaseHelper = DatabaseHelper(this)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val tombolMasuk = findViewById<AppCompatButton>(R.id.btn_masuk)
        val tvLupaPassword = findViewById<TextView>(R.id.tv_lupa_password)

        tvLupaPassword.setOnClickListener {
            startActivity(Intent(this, NamaEmailActivity::class.java))
        }

        tombolMasuk.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Mohon isi email Anda", Toast.LENGTH_SHORT).show()
            } else {
                val prosesBerhasil = databaseHelper.simpanAtauCekUser(email)
                if (prosesBerhasil) {
                    Toast.makeText(this, "Selamat Datang!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Kesalahan database", Toast.LENGTH_SHORT).show()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}