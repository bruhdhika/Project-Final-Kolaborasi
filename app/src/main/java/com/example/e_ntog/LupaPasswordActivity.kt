package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LupaPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lupa_password)

        val btnKirim = findViewById<Button>(R.id.btn_kirim)

        btnKirim?.setOnClickListener {
            Toast.makeText(this, "Kata Sandi Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()

            // Balik ke SignInActivity dan bersihkan history halaman sebelumnya
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}