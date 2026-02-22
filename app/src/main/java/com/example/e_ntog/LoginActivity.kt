package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        val btnSignUp = findViewById<Button>(R.id.btn_sign_up)

        // Pindah ke halaman Sign In (layout yang ada Email & Password)
        btnSignIn.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignInActivity::class.java)
            startActivity(intent)
        }

        // Pindah ke halaman Sign Up (Daftar)
        btnSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}