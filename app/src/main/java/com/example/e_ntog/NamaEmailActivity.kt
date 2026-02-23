package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class NamaEmailActivity : BaseActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_namaemail)
setupBackButton()
        val etEmail    = findViewById<EditText>(R.id.et_email_otp)
        val btnKirim   = findViewById<Button>(R.id.btn_kirim_otp)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        btnKirim.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) { etEmail.error = "Email wajib diisi"; return@setOnClickListener }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"; return@setOnClickListener
            }

            btnKirim.isEnabled = false
            progressBar?.visibility = View.VISIBLE

            // Kirim email reset password via Firebase
            // Firebase akan mengirim link reset ke email tsb.
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    progressBar?.visibility = View.GONE
                    // Lanjut ke halaman OTP (untuk UX, meski Firebase pakai link, bukan OTP)
                    val intent = Intent(this, OtpActivity::class.java)
                    intent.putExtra("USER_EMAIL", email)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    progressBar?.visibility = View.GONE
                    btnKirim.isEnabled = true
                    val msg = when {
                        it.message?.contains("no user") == true ->
                            "Email tidak terdaftar di sistem kami."
                        else -> "Gagal kirim email: ${it.message}"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
        }
    }
}
