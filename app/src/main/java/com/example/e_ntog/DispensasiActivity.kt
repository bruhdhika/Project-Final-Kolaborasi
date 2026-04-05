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

class DispensasiActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private lateinit var waliLauncher: ActivityResultLauncher<Intent>

    private lateinit var etNama: EditText
    private lateinit var spinnerKelas: Spinner
    private lateinit var etAlasan: EditText
    private lateinit var etWaliKelas: EditText
    private lateinit var clSearchWali: ConstraintLayout
    private lateinit var btnSubmit: AppCompatButton

    private var muridKelasId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispensasi)
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

        if (isGuru) {
            spinnerKelas.visibility = View.GONE
            clSearchWali.visibility = View.GONE
            findViewById<View?>(R.id.tv_label_kelas)?.visibility = View.GONE
            findViewById<View?>(R.id.tv_label_wali)?.visibility  = View.GONE
            muridKelasId = ""
        } else {
            spinnerKelas.visibility = View.VISIBLE
            clSearchWali.visibility = View.VISIBLE

            loadKelasSpinner()

            waliLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val namaWali = result.data?.getStringExtra("NAMA_WALI_TERPILIH")
                    etWaliKelas.setText(namaWali)
                    clSearchWali.setBackgroundResource(R.drawable.bg_edittext_green)
                    etWaliKelas.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_circle, 0)
                    etWaliKelas.setPadding(16, 0, 16, 0)
                }
            }
            val openWali = { waliLauncher.launch(Intent(this, WaliKelasActivity::class.java)) }
            clSearchWali.setOnClickListener { openWali() }
            findViewById<ImageView>(R.id.iv_search_icon).setOnClickListener { openWali() }
        }

        btnSubmit.setOnClickListener {
            val nama   = etNama.text.toString().trim()
            val alasan = etAlasan.text.toString().trim()
            if (nama.isEmpty())   { etNama.error = "Nama wajib diisi";   return@setOnClickListener }
            if (alasan.isEmpty()) { etAlasan.error = "Alasan wajib diisi"; return@setOnClickListener }

            val kelas: String
            val wali: String

            if (isGuru) {
                kelas = "Guru"
                wali  = "-"
            } else {
                kelas = spinnerKelas.selectedItem?.toString() ?: ""
                wali  = etWaliKelas.text.toString().trim()
                if (kelas.isEmpty() || kelas == "Belum join kelas") {
                    Toast.makeText(this, "Kamu belum join kelas", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (wali.isEmpty()) {
                    Toast.makeText(this, "Pilih wali kelas terlebih dahulu", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            btnSubmit.isEnabled = false
            val uid     = session.getUid()
            val tanggal = SimpleDateFormat("dd MMMM yyyy", Locale("id")).format(Date())

            val historyData = hashMapOf(
                "nama"      to nama,
                "kelas"     to kelas,
                "alasan"    to alasan,
                "waliKelas" to wali,
                "tanggal"   to tanggal,
                "timestamp" to Timestamp.now(),
                "status"    to "pending"
            )

            db.collection("users").document(uid)
                .collection("history_dispen")
                .add(historyData)
                .addOnSuccessListener {
                    db.collection("users").document(uid)
                        .update("totalDispen", FieldValue.increment(1))

                    if (!isGuru && muridKelasId.isNotEmpty()) {
                        ForumKelasActivity.kirimPesanSistem(
                            db, muridKelasId,
                            "📋 $nama mengajukan DISPENSASI pada $tanggal. Alasan: $alasan"
                        )
                    }

                    btnSubmit.isEnabled = true
                    startActivity(Intent(this, StrukDispensasi::class.java).apply {
                        putExtra("NAMA", nama)
                        putExtra("KELAS", kelas)
                        putExtra("ALASAN", alasan)
                        putExtra("WALI_KELAS", wali)
                    })
                    finish()
                }
                .addOnFailureListener {
                    btnSubmit.isEnabled = true
                    Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadKelasSpinner() {
        db.collection("users").document(session.getUid()).get()
            .addOnSuccessListener { userDoc ->
                val kelasId   = userDoc.getString("kelasId")   ?: ""
                val kelasNama = userDoc.getString("kelasNama") ?: ""
                muridKelasId  = kelasId

                if (kelasId.isEmpty()) {
                    spinnerKelas.adapter = ArrayAdapter(this,
                        android.R.layout.simple_spinner_item, listOf("Belum join kelas"))
                        .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                    return@addOnSuccessListener
                }
                db.collection("kelas").document(kelasId).get()
                    .addOnSuccessListener { kelasDoc ->
                        val nama = kelasDoc.getString("namaKelas") ?: kelasNama
                        spinnerKelas.adapter = ArrayAdapter(this,
                            android.R.layout.simple_spinner_item, listOf(nama))
                            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                    }
            }
    }
}
