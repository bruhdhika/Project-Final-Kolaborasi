package com.example.e_ntog

import android.app.Activity // <-- IMPORT
import android.content.Intent
import android.os.Bundle
import android.widget.EditText // <-- IMPORT
import android.widget.Spinner // <-- IMPORT
import androidx.activity.result.ActivityResultLauncher // <-- IMPORT
import androidx.activity.result.contract.ActivityResultContracts // <-- IMPORT
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton // <-- IMPORT
import androidx.constraintlayout.widget.ConstraintLayout

class TerlambatActivity : AppCompatActivity() {

    // Definisikan Launcher
    private lateinit var waliKelasResultLauncher: ActivityResultLauncher<Intent>

    // Definisikan komponen form di level atas agar bisa diakses di mana-mana
    private lateinit var etNama: EditText
    private lateinit var spinnerKelas: Spinner
    private lateinit var etAlasan: EditText
    private lateinit var etWaliKelas: EditText
    private lateinit var clSearchWali: ConstraintLayout
    private lateinit var btnSubmit: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terlambat)

        // Inisialisasi semua komponen
        etNama = findViewById(R.id.et_nama)
        spinnerKelas = findViewById(R.id.spinner_kelas)
        etAlasan = findViewById(R.id.et_alasan)
        etWaliKelas = findViewById(R.id.et_wali_kelas)
        clSearchWali = findViewById(R.id.cl_search_wali)
        btnSubmit = findViewById(R.id.btn_submit)

        // --- REVISI UNTUK MENERIMA HASIL ---
        waliKelasResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Cek apakah hasilnya OK
            if (result.resultCode == Activity.RESULT_OK) {
                val namaWali = result.data?.getStringExtra("NAMA_WALI_TERPILIH")

                // 1. Set teks di EditText
                etWaliKelas.setText(namaWali)

                // 2. Ubah latar belakang jadi hijau
                clSearchWali.setBackgroundResource(R.drawable.bg_edittext_green)

                // 3. Ubah ikon search jadi ikon ceklis
                // (Kita perlu ID untuk ImageView di dalam cl_search_wali)
                // Buka activity_terlambat.xml dan tambahkan android:id="@+id/iv_search_icon"
                // ke ImageView di dalam cl_search_wali

                // NOTE: Untuk sementara, kita ganti drawable di EditText-nya saja
                etWaliKelas.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_circle, 0)
                etWaliKelas.setPadding(16,0,16,0) // sesuaikan padding
            }
        }
        // --- Akhir Revisi ---

        // Listener untuk membuka halaman Wali Kelas
        clSearchWali.setOnClickListener {
            val intent = Intent(this, WaliKelasActivity::class.java)
            waliKelasResultLauncher.launch(intent) // Gunakan Launcher
        }

        // --- REVISI UNTUK TOMBOL SUBMIT ---
        btnSubmit.setOnClickListener {
            // 1. Ambil semua data dari form
            val nama = etNama.text.toString()
            val kelas = spinnerKelas.selectedItem.toString()
            val alasan = etAlasan.text.toString()
            val waliKelas = etWaliKelas.text.toString() // Ambil nama yg sudah dipilih

            // (Opsional: Tambah validasi di sini, pastikan tidak ada yg kosong)

            // 2. Buat Intent untuk pindah ke StrukActivity
            val intent = Intent(this, StrukActivity::class.java)

            // 3. Masukkan semua data ke Intent
            intent.putExtra("NAMA", nama)
            intent.putExtra("KELAS", kelas)
            intent.putExtra("ALASAN", alasan)
            intent.putExtra("WALI_KELAS", waliKelas)

            // 4. Pindah ke Halaman Struk
            startActivity(intent)
        }
    }
}