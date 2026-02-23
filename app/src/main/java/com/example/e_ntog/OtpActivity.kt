package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * OtpActivity: Memberitahu user bahwa email reset sudah dikirim.
 * User diminta cek email lalu klik link dari Firebase.
 * Setelah klik link, user bisa login ulang dengan password baru.
 */
class OtpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
setupBackButton()
        val emailTerima  = intent.getStringExtra("USER_EMAIL") ?: ""
        val btnKirimOtp  = findViewById<Button>(R.id.btn_kirim_otp)

        // Tampilkan info ke user bahwa link sudah dikirim
        Toast.makeText(
            this,
            "Link reset password dikirim ke $emailTerima. Cek inbox/spam kamu!",
            Toast.LENGTH_LONG
        ).show()

        // Tombol 'Kirim' = kembali ke halaman Sign In
        btnKirimOtp.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}
