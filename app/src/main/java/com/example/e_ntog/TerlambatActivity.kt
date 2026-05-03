package com.example.e_ntog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TerlambatActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager

    private lateinit var etNama: EditText
    private lateinit var spinnerKelas: Spinner
    private lateinit var etAlasan: EditText
    private lateinit var etWaliKelas: EditText
    private lateinit var clSearchWali: ConstraintLayout
    private lateinit var btnSubmit: AppCompatButton

    private lateinit var waliLauncher: ActivityResultLauncher<Intent>
    private var muridKelasId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terlambat)
        setupBackButton()
        session = SessionManager(this)

        // Inisialisasi View
        etNama       = findViewById(R.id.et_nama)
        spinnerKelas = findViewById(R.id.spinner_kelas)
        etAlasan     = findViewById(R.id.et_alasan)
        etWaliKelas  = findViewById(R.id.et_wali_kelas)
        clSearchWali = findViewById(R.id.cl_search_wali)
        btnSubmit    = findViewById(R.id.btn_submit)

        etNama.setText(session.getNama())
        val isGuru = session.getRole() == SessionManager.ROLE_GURU

        if (!isGuru) {
            loadKelasSpinner()

            waliLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val namaWali = result.data?.getStringExtra("NAMA_WALI_TERPILIH")
                    etWaliKelas.setText(namaWali)
                }
            }

            val openWali = { waliLauncher.launch(Intent(this, WaliKelasActivity::class.java)) }
            clSearchWali.setOnClickListener { openWali() }
            etWaliKelas.setOnClickListener { openWali() }
        } else {
            spinnerKelas.visibility = View.GONE
            clSearchWali.visibility = View.GONE
        }

        // --- TOMBOL SUBMIT ---
        btnSubmit.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val alasan = etAlasan.text.toString().trim()
            val wali = etWaliKelas.text.toString().trim()
            val kelas = spinnerKelas.selectedItem?.toString() ?: ""

            if (nama.isEmpty() || alasan.isEmpty() || (!isGuru && wali.isEmpty())) {
                Toast.makeText(this, "Data belum lengkap!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false

            val data = hashMapOf(
                "nama" to nama,
                "kelas" to kelas,
                "alasan" to alasan,
                "waliKelas" to wali,
                "tipe" to "TERLAMBAT",
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("laporan_terlambat").add(data).addOnSuccessListener {
                // Pindah ke Struk
                val intent = Intent(this, StrukActivity::class.java)
                intent.putExtra("NAMA", nama)
                intent.putExtra("KELAS", kelas)
                intent.putExtra("ALASAN", alasan)
                intent.putExtra("WALI_KELAS", wali)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                btnSubmit.isEnabled = true
                Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    } // Akhir onCreate

    private fun loadKelasSpinner() {
        db.collection("users").document(session.getUid()).get().addOnSuccessListener { userDoc ->
            val kelasId = userDoc.getString("kelasId") ?: ""
            muridKelasId = kelasId

            if (kelasId.isEmpty()) {
                spinnerKelas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Belum join kelas"))
                return@addOnSuccessListener
            }

            db.collection("kelas").document(kelasId).get().addOnSuccessListener { kelasDoc ->
                val nama = kelasDoc.getString("namaKelas") ?: "Kelas"
                spinnerKelas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(nama))
            }
        }
    }
}