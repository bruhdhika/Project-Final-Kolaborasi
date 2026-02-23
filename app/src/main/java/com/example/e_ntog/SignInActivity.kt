package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : BaseActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
setupBackButton()
        session = SessionManager(this)

        val etEmail      = findViewById<EditText>(R.id.et_email)
        val etPassword   = findViewById<EditText>(R.id.et_password)
        val btnMasuk     = findViewById<Button>(R.id.btn_masuk)
        val tvLupa       = findViewById<TextView>(R.id.tv_lupa_password)
        val progressBar  = findViewById<ProgressBar>(R.id.progress_bar)

        tvLupa.setOnClickListener {
            startActivity(Intent(this, NamaEmailActivity::class.java))
        }

        btnMasuk.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass  = etPassword.text.toString().trim()

            // Validasi input
            if (email.isEmpty()) { etEmail.error = "Email wajib diisi"; return@setOnClickListener }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"; return@setOnClickListener
            }
            if (pass.isEmpty())  { etPassword.error = "Password wajib diisi"; return@setOnClickListener }
            if (pass.length < 6) { etPassword.error = "Password minimal 6 karakter"; return@setOnClickListener }

            btnMasuk.isEnabled = false
            progressBar.visibility = View.VISIBLE

            // Login dengan Firebase Auth
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    // Ambil profil dari Firestore
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val nama = doc.getString("nama") ?: email
                            val role = doc.getString("role") ?: SessionManager.ROLE_MURID
                            session.saveSession(uid, email, nama, role)
                            progressBar.visibility = View.GONE
                            startActivity(Intent(this, HomeActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            btnMasuk.isEnabled = true
                            Toast.makeText(this, "Gagal ambil profil: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    btnMasuk.isEnabled = true
                    Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
