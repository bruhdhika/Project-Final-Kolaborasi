package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.text.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class JoinKelasActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager

    // 6 kotak input kode
    private lateinit var etBoxes: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_kelas)
setupBackButton()
        session = SessionManager(this)

        val tvSambutan = findViewById<TextView>(R.id.tv_sambutan_join)
        val btnJoin    = findViewById<Button>(R.id.btn_join_kelas)
        val progressBar= findViewById<ProgressBar>(R.id.progress_bar_join)

        tvSambutan.text = "Hai ${session.getNama()},\nMasukkan kode kelas dari gurumu!"

        // Inisialisasi 6 kotak input
        etBoxes = arrayOf(
            findViewById(R.id.et_kode_1), findViewById(R.id.et_kode_2),
            findViewById(R.id.et_kode_3), findViewById(R.id.et_kode_4),
            findViewById(R.id.et_kode_5), findViewById(R.id.et_kode_6)
        )

        // Auto-focus antar kotak saat ketik
        setupAutoFocus()

        btnJoin.setOnClickListener {
            val kode = etBoxes.joinToString("") { it.text.toString().trim().uppercase() }

            if (kode.length < 6) {
                Toast.makeText(this, "Masukkan 6 digit kode kelas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnJoin.isEnabled = false
            progressBar.visibility = View.VISIBLE

            // Cari kelas dengan kode ini di Firestore
            db.collection("kelas")
                .whereEqualTo("kodeKelas", kode)
                .limit(1)
                .get()
                .addOnSuccessListener { snaps ->
                    if (snaps.isEmpty) {
                        progressBar.visibility = View.GONE
                        btnJoin.isEnabled = true
                        Toast.makeText(this,
                            "Kode kelas tidak ditemukan. Cek ulang kode dari gurumu.",
                            Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    val kelasDoc  = snaps.documents[0]
                    val kelasId   = kelasDoc.id
                    val kelasNama = kelasDoc.getString("namaKelas") ?: ""

                    // Update dokumen murid: simpan kelasId & kelasNama
                    val uid = session.getUid()
                    db.collection("users").document(uid)
                        .update(
                            "kelasId",   kelasId,
                            "kelasNama", kelasNama
                        )
                        .addOnSuccessListener {
                            // Increment jumlahMurid di dokumen kelas
                            db.collection("kelas").document(kelasId)
                                .update("jumlahMurid", FieldValue.increment(1))

                            progressBar.visibility = View.GONE
                            Toast.makeText(this,
                                "Berhasil masuk kelas $kelasNama! 🎉",
                                Toast.LENGTH_LONG).show()

                            // Ke HomeActivity (sekarang sudah punya kelas)
                            startActivity(Intent(this, HomeActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            btnJoin.isEnabled = true
                            Toast.makeText(this, "Gagal join kelas: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    btnJoin.isEnabled = true
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Auto-focus: setelah isi 1 kotak, kursor otomatis pindah ke kotak berikutnya.
     * Jika delete di kotak kosong, kursor balik ke kotak sebelumnya.
     */
    private fun setupAutoFocus() {
        for (i in etBoxes.indices) {
            etBoxes[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < etBoxes.size - 1) {
                        etBoxes[i + 1].requestFocus()
                    }
                }
            })

            etBoxes[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                    event.action == android.view.KeyEvent.ACTION_DOWN &&
                    etBoxes[i].text.isEmpty() && i > 0) {
                    etBoxes[i - 1].requestFocus()
                    etBoxes[i - 1].setText("")
                    true
                } else false
            }
        }
    }
}
