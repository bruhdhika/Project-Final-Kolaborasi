package com.example.e_ntog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TidakHadirActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private lateinit var waliLauncher: ActivityResultLauncher<Intent>

    private lateinit var etNama: EditText
    private lateinit var spinnerKelas: Spinner
    private lateinit var etAlasan: EditText
    private lateinit var etWaliKelas: EditText
    private lateinit var clSearchWali: ConstraintLayout
    private lateinit var ivSearchWali: ImageView
    private lateinit var btnSubmit: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tidak_hadir)
setupBackButton()
        session = SessionManager(this)

        etNama      = findViewById(R.id.et_nama)
        spinnerKelas= findViewById(R.id.spinner_kelas)
        etAlasan    = findViewById(R.id.et_alasan)
        etWaliKelas = findViewById(R.id.et_wali_kelas)
        clSearchWali= findViewById(R.id.cl_search_wali)
        ivSearchWali= findViewById(R.id.iv_search_icon)
        btnSubmit   = findViewById(R.id.btn_submit)

        etNama.setText(session.getNama())

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
        ivSearchWali.setOnClickListener { openWali() }

        btnSubmit.setOnClickListener {
            val nama   = etNama.text.toString().trim()
            val kelas  = spinnerKelas.selectedItem.toString()
            val alasan = etAlasan.text.toString().trim()
            val wali   = etWaliKelas.text.toString().trim()

            if (nama.isEmpty())   { etNama.error = "Nama wajib diisi"; return@setOnClickListener }
            if (kelas == "Pilih Kelas...") {
                Toast.makeText(this, "Pilih kelas terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (alasan.isEmpty()) { etAlasan.error = "Alasan wajib diisi"; return@setOnClickListener }
            if (wali.isEmpty())   {
                Toast.makeText(this, "Pilih wali kelas terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            val uid     = session.getUid()
            val tanggal = SimpleDateFormat("dd MMMM yyyy", Locale("id")).format(Date())

            val historyData = hashMapOf(
                "nama" to nama, "kelas" to kelas, "alasan" to alasan,
                "waliKelas" to wali, "tanggal" to tanggal, "timestamp" to "Timestamp.now()",
                 "status"  to "pending"
            )

            db.collection("users").document(uid)
                .collection("history_tidak_hadir").add(historyData)
                .addOnSuccessListener {
                    db.collection("users").document(uid)
                        .update("totalTidakHadir", FieldValue.increment(1))
                    btnSubmit.isEnabled = true
                    startActivity(Intent(this, StrukTidakHadir::class.java).apply {
                        putExtra("NAMA", nama); putExtra("KELAS", kelas)
                        putExtra("ALASAN", alasan); putExtra("WALI_KELAS", wali)
                    })
                    finish()
                }
                .addOnFailureListener {
                    btnSubmit.isEnabled = true
                    Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
