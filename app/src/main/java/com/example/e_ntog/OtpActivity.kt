package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OtpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        val etDisplayEmail = findViewById<EditText>(R.id.et_email_otp)
        val btnKirimOtp = findViewById<Button>(R.id.btn_kirim_otp)

        // Ambil data dari Intent
        val emailTerima = intent.getStringExtra("USER_EMAIL")

        // Pastikan view tidak null sebelum set text
        if (etDisplayEmail != null && emailTerima != null) {
            etDisplayEmail.setText(emailTerima)
        }

        btnKirimOtp?.setOnClickListener {
            try {
                // Sesuai alur: Setelah OTP lanjut ke LupaPassword (Reset Sandi)
                val intent = Intent(this, LupaPasswordActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal pindah halaman: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}