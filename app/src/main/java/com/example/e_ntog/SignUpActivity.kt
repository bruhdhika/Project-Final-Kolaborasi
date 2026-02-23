package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : BaseActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
setupBackButton()
        session = SessionManager(this)

        val etNama      = findViewById<EditText>(R.id.et_nama)
        val etEmail     = findViewById<EditText>(R.id.et_email)
        val etPassword  = findViewById<EditText>(R.id.et_password)
        val btnDaftar   = findViewById<Button>(R.id.btn_daftar)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        btnDaftar.setOnClickListener {
            val nama  = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass  = etPassword.text.toString().trim()

            // Validasi input
            if (nama.isEmpty())  { etNama.error = "Nama wajib diisi"; return@setOnClickListener }
            if (email.isEmpty()) { etEmail.error = "Email wajib diisi"; return@setOnClickListener }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"; return@setOnClickListener
            }
            if (pass.isEmpty())  { etPassword.error = "Password wajib diisi"; return@setOnClickListener }
            if (pass.length < 6) { etPassword.error = "Password minimal 6 karakter"; return@setOnClickListener }

            btnDaftar.isEnabled = false
            progressBar.visibility = View.VISIBLE

            // Buat akun Firebase Auth
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    // Cek apakah email ini terdaftar sebagai guru (di collection guru_accounts)
                    db.collection("guru_accounts").document(email).get()
                        .addOnSuccessListener { guruDoc ->
                            val role = if (guruDoc.exists()) SessionManager.ROLE_GURU
                            else SessionManager.ROLE_MURID

                            // Simpan profil user ke Firestore
                            val userData = hashMapOf(
                                "uid"         to uid,
                                "nama"        to nama,
                                "email"       to email,
                                "role"        to role,
                                "kelas"       to "",
                                "nis"         to "",
                                "photoUrl"    to "",
                                "totalTerlambat" to 0,
                                "totalTidakHadir" to 0,
                                "totalDispen" to 0,
                                "createdAt"   to FieldValue.serverTimestamp()
                            )

                            db.collection("users").document(uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    session.saveSession(uid, email, nama, role)
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, HomeActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    })
                                }
                                .addOnFailureListener {
                                    progressBar.visibility = View.GONE
                                    btnDaftar.isEnabled = true
                                    Toast.makeText(this, "Gagal simpan profil: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    btnDaftar.isEnabled = true
                    val msg = when {
                        it.message?.contains("email address is already") == true ->
                            "Email sudah terdaftar. Silakan login."
                        else -> "Pendaftaran gagal: ${it.message}"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
        }
    }
}
