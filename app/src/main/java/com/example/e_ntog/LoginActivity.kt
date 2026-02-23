package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity() {

    private lateinit var session: SessionManager
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = SessionManager(this)
        setupBackButton()
        // Jika sudah login (Firebase Auth masih aktif & session ada), langsung ke Home
        if (auth.currentUser != null && session.isLoggedIn()) {
            goToHome()
            return
        }

        setContentView(R.layout.activity_login)

        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        val btnSignUp = findViewById<Button>(R.id.btn_sign_up)

        btnSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
