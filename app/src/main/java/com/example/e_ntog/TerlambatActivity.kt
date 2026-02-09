package com.example.e_ntog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast // Penting untuk Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout

class TerlambatActivity : AppCompatActivity() {

    // 1. Deklarasi Variabel Global di dalam Class
    private lateinit var waliKelasResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var etNama: EditText
    private lateinit var spinnerKelas: Spinner
    private lateinit var etAlasan: EditText
    private lateinit var etWaliKelas: EditText
    private lateinit var clSearchWali: ConstraintLayout
    private lateinit var btnSubmit: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terlambat)

        // 2. Inisialisasi Database
        databaseHelper = DatabaseHelper(this)

        // 3. Inisialisasi Views (Cukup sekali di sini)
        etNama = findViewById(R.id.et_nama)
        spinnerKelas = findViewById(R.id.spinner_kelas)
        etAlasan = findViewById(R.id.et_alasan)
        etWaliKelas = findViewById(R.id.et_wali_kelas)
        clSearchWali = findViewById(R.id.cl_search_wali)
        btnSubmit = findViewById(R.id.btn_submit)

        // 4. Setup Result Launcher (Untuk menerima data dari WaliKelasActivity)
        waliKelasResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val namaWali = result.data?.getStringExtra("NAMA_WALI_TERPILIH")

                // Update UI jika wali dipilih
                etWaliKelas.setText(namaWali)
                clSearchWali.setBackgroundResource(R.drawable.bg_edittext_green) // Pastikan drawable ini ada, atau ganti warna

                // Opsional: Menambah ikon centang (pastikan ic_check_circle ada di drawable)
                // etWaliKelas.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_circle, 0)
            }
        }

        // 5. Listener Tombol Cari Wali Kelas
        clSearchWali.setOnClickListener {
            val intent = Intent(this, WaliKelasActivity::class.java)
            waliKelasResultLauncher.launch(intent)
        }

        // 6. Listener Tombol Submit (Logika Gabungan)
        btnSubmit.setOnClickListener {
            // A. Ambil Data dari Input
            val nama = etNama.text.toString().trim()
            val kelas = spinnerKelas.selectedItem.toString()
            val alasan = etAlasan.text.toString().trim()
            val wali = etWaliKelas.text.toString().trim()

            // B. Validasi Input Kosong
            if (nama.isEmpty() || alasan.isEmpty() || wali.isEmpty()) {
                Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
            } else {
                // C. Simpan ke Database SQLite
                val isSavedToDB = databaseHelper.insertTerlambat(nama, kelas, alasan, wali)

                if (isSavedToDB) {
                    // D. Jika Database Berhasil -> Simpan Shared Preferences (Counter)
                    val prefs = getSharedPreferences("DATA_IZIN", MODE_PRIVATE)
                    val editor = prefs.edit()
                    val jumlahTerlambat = prefs.getInt("JUMLAH_IZIN_TERLAMBAT", 0) + 1
                    editor.putInt("JUMLAH_IZIN_TERLAMBAT", jumlahTerlambat)
                    editor.apply()

                    Toast.makeText(this, "Data Berhasil Disimpan!", Toast.LENGTH_SHORT).show()

                    // E. Pindah ke StrukActivity
                    val intent = Intent(this, StrukActivity::class.java)
                    intent.putExtra("NAMA", nama)
                    intent.putExtra("KELAS", kelas)
                    intent.putExtra("ALASAN", alasan)
                    intent.putExtra("WALI_KELAS", wali)
                    startActivity(intent)

                    // F. Tutup Activity ini agar tidak bisa back
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menyimpan ke database", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}