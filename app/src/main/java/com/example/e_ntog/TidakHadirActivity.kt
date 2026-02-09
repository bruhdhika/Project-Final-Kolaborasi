package com.example.e_ntog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.ImageView

class TidakHadirActivity : AppCompatActivity() {

    // Launcher
    private lateinit var waliKelasResultLauncher: ActivityResultLauncher<Intent>

    // Komponen form
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

        // Inisialisasi komponen
        etNama = findViewById(R.id.et_nama)
        spinnerKelas = findViewById(R.id.spinner_kelas)
        etAlasan = findViewById(R.id.et_alasan)
        etWaliKelas = findViewById(R.id.et_wali_kelas)
        clSearchWali = findViewById(R.id.cl_search_wali)
        ivSearchWali = findViewById(R.id.iv_search_icon)   // <-- DITAMBAHKAN
        btnSubmit = findViewById(R.id.btn_submit)

        // Launcher terima hasil
        waliKelasResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == Activity.RESULT_OK) {

                    val namaWali = result.data?.getStringExtra("NAMA_WALI_TERPILIH")

                    etWaliKelas.setText(namaWali)

                    clSearchWali.setBackgroundResource(R.drawable.bg_edittext_green)

                    etWaliKelas.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_check_circle, 0
                    )
                    etWaliKelas.setPadding(16, 0, 16, 0)
                }
            }

        // Ke Wali Kelas
        val openWali = {
            val intent = Intent(this, WaliKelasActivity::class.java)
            waliKelasResultLauncher.launch(intent)
        }

        clSearchWali.setOnClickListener { openWali() }
        ivSearchWali.setOnClickListener { openWali() }   // <-- FIX: klik ikon search sekarang bisa
        // ---------------------------------

        // Tombol Submit
        btnSubmit.setOnClickListener {

            val nama = etNama.text.toString()
            val kelas = spinnerKelas.selectedItem.toString()
            val alasan = etAlasan.text.toString()
            val waliKelas = etWaliKelas.text.toString()

            val intent = Intent(this, StrukTidakHadir::class.java)

            intent.putExtra("NAMA", nama)
            intent.putExtra("KELAS", kelas)
            intent.putExtra("ALASAN", alasan)
            intent.putExtra("WALI_KELAS", waliKelas)

            startActivity(intent)

            //Simpen data dispen
            val prefs = getSharedPreferences("DATA_IZIN", MODE_PRIVATE)
            val editor = prefs.edit()

            val jumlahIzin = prefs.getInt("JUMLAH_IZIN_TIDAK_MASUK", 0) + 1
            editor.putInt("JUMLAH_IZIN_TIDAK_MASUK", jumlahIzin)

            editor.apply()

            //Balik ke Home
            finish()

        }
    }
}
