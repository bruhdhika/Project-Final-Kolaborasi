package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
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
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper(this)

        val etEmail = findViewById<EditText>(R.id.et_email)
        // val etPassword = findViewById<EditText>(R.id.et_password) // Password tidak dipakai

        val tombolMasuk = findViewById<AppCompatButton>(R.id.btn_masuk)

        tombolMasuk.setOnClickListener {
            val email = etEmail.text.toString().trim()

            // Validasi: Hanya cek Email saja
            if (email.isEmpty()) {
                Toast.makeText(this, "Mohon isi email Anda", Toast.LENGTH_SHORT).show()
            } else {
                // Panggil fungsi "Simpan atau Cek"
                val prosesBerhasil = databaseHelper.simpanAtauCekUser(email)

                if (prosesBerhasil) {
                    Toast.makeText(this, "Selamat Datang!", Toast.LENGTH_SHORT).show()

                    // Pindah ke Home
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Terjadi kesalahan database", Toast.LENGTH_SHORT).show()
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