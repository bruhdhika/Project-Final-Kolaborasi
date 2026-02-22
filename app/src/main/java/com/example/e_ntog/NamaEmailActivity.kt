package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NamaEmailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_namaemail)

        // Gunakan safe call (asumsikan ID di XML adalah et_email_otp dan btn_kirim_otp)
        val etEmail = findViewById<EditText>(R.id.et_email_otp)
        val btnKirim = findViewById<Button>(R.id.btn_kirim_otp)

        btnKirim?.setOnClickListener {
            val email = etEmail?.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else if (!email.endsWith("@gmail.com")) {
                Toast.makeText(this, "Gunakan format @gmail.com", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val intent = Intent(this, OtpActivity::class.java)
                    intent.putExtra("USER_EMAIL", email)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}