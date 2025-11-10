package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.EditText // <-- IMPORT INI
import android.widget.Toast // <-- IMPORT INI
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // --- KODE REVISI DIMULAI DI SINI ---

        // 1. Temukan semua komponen yang kita butuhkan
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val tombolMasuk = findViewById<AppCompatButton>(R.id.btn_masuk)

        // 2. Beri 'onClick' listener
        tombolMasuk.setOnClickListener {
            // 3. Ambil teks dari EditText SETIAP KALI tombol diklik
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 4. Lakukan pengecekan
            if (email.isEmpty() || password.isEmpty()) {
                // JIKA KOSONG: Tampilkan pop-up (Toast)
                Toast.makeText(this, "Selesaikan login", Toast.LENGTH_SHORT).show()
            } else {
                // JIKA TERISI: Pindah halaman
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                startActivity(intent)
            }
        }

        // --- KODE REVISI SELESAI ---

        // Kode bawaan untuk 'edge to edge' (biarkan saja)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}