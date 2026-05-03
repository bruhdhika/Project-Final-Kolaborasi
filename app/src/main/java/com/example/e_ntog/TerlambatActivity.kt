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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TerlambatActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager

    private lateinit var etNama: EditText
    private lateinit var spinnerKelas: Spinner
    private lateinit var etAlasan: EditText
    private lateinit var etWaliKelas: EditText
    private lateinit var clSearchWali: ConstraintLayout
    private lateinit var btnSubmit: AppCompatButton

    private lateinit var waliLauncher: ActivityResultLauncher<Intent> // ✅ FIX

    private var muridKelasId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terlambat)
        setupBackButton()
        session = SessionManager(this)

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

            waliLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val namaWali = result.data?.getStringExtra("NAMA_WALI_TERPILIH")
                    etWaliKelas.setText(namaWali)
                }
            }

            val openWali = {
                waliLauncher.launch(Intent(this, WaliKelasActivity::class.java))
            }

            clSearchWali.setOnClickListener { openWali() }
            findViewById<ImageView>(R.id.iv_search_icon)
                .setOnClickListener { openWali() }
        } else {
            spinnerKelas.visibility = View.GONE
            clSearchWali.visibility = View.GONE
        }

        btnSubmit.setOnClickListener {
            // (biarin sama seperti punyamu, ini nggak error)
        }
    }

    private fun loadKelasSpinner() {
        db.collection("users").document(session.getUid()).get()
            .addOnSuccessListener { userDoc ->
                val kelasId   = userDoc.getString("kelasId")   ?: ""
                val kelasNama = userDoc.getString("kelasNama") ?: ""
                muridKelasId  = kelasId

                if (kelasId.isEmpty()) {
                    spinnerKelas.adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        listOf("Belum join kelas")
                    )
                    return@addOnSuccessListener
                }

                db.collection("kelas").document(kelasId).get()
                    .addOnSuccessListener { kelasDoc ->
                        val nama = kelasDoc.getString("namaKelas") ?: kelasNama
                        spinnerKelas.adapter = ArrayAdapter(
                            this,
                            android.R.layout.simple_spinner_item,
                            listOf(nama)
                        )
                    }
            }
    }
}