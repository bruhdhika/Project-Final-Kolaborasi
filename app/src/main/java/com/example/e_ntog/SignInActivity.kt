package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView // WAJIB ADA AGAR TIDAK ERROR
import androidx.appcompat.app.AppCompatActivity

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val btnMasuk = findViewById<Button>(R.id.btn_masuk)

        // Sekarang ini tidak akan error jika ID di XML sudah ditambahkan
        val tvLupaPassword = findViewById<TextView>(R.id.tv_lupa_password)

        btnMasuk.setOnClickListener {
            val intent = Intent(this@SignInActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        tvLupaPassword.setOnClickListener {
            val intent = Intent(this@SignInActivity, NamaEmailActivity::class.java)
            startActivity(intent)
        }
    }
}