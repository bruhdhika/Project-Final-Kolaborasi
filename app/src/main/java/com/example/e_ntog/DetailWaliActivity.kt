package com.example.e_ntog

import android.app.Activity // <-- IMPORT INI
import android.content.Intent // <-- IMPORT INI
import android.graphics.Color
import android.os.Bundle
import android.widget.Button // <-- IMPORT INI
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton // <-- IMPORT INI
import androidx.constraintlayout.widget.ConstraintLayout

class DetailWaliActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_wali)

        // 1. Ambil data yang dikirim dari Intent
        val namaDepan = intent.getStringExtra("NAMA_DEPAN")
        val namaBelakang = intent.getStringExtra("NAMA_BELAKANG")
        val fotoResId = intent.getIntExtra("FOTO_ID", R.drawable.boy)
        val warnaBg = intent.getStringExtra("WARNA_BG") ?: "#F5F5F5"

        // 2. Temukan komponen di layout
        val tvNamaDepan = findViewById<TextView>(R.id.tv_nama_depan)
        val tvNamaBelakang = findViewById<TextView>(R.id.tv_nama_belakang)
        val ivFotoDetail = findViewById<ImageView>(R.id.iv_wali_photo_detail)
        val clBackground = findViewById<ConstraintLayout>(R.id.cl_wali_background)
        val backButton = findViewById<ImageView>(R.id.iv_back_arrow)
        val btnKirimPesan = findViewById<AppCompatButton>(R.id.btn_kirim_pesan) // <-- TEMUKAN TOMBOL

        // 3. Set data ke komponen
        tvNamaDepan.text = namaDepan
        tvNamaBelakang.text = namaBelakang
        ivFotoDetail.setImageResource(fotoResId)
        clBackground.setBackgroundColor(Color.parseColor(warnaBg))

        // 4. Aktifkan tombol back
        backButton.setOnClickListener {
            finish()
        }

        // 5. Aktifkan tombol "Kirim Pesan"
        btnKirimPesan.setOnClickListener {
            // Buat Intent untuk "hasil"
            val resultIntent = Intent()
            // Masukkan nama lengkap wali kelas ke dalam hasil
            val namaWaliLengkap = "$namaDepan $namaBelakang"
            resultIntent.putExtra("NAMA_WALI_TERPILIH", namaWaliLengkap)

            // Set hasilnya sebagai "OK" dan kirim datanya
            setResult(Activity.RESULT_OK, resultIntent)

            // Tutup halaman ini
            finish()
        }
    }
}