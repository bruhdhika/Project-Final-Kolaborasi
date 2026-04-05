package com.example.e_ntog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.bumptech.glide.Glide

class DetailWaliActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_wali)
        setupBackButton()

        val guruUid   = intent.getStringExtra("GURU_UID")   ?: ""
        val guruNama  = intent.getStringExtra("GURU_NAMA")  ?: "-"
        val guruKelas = intent.getStringExtra("GURU_KELAS") ?: "-"
        val guruPhoto = intent.getStringExtra("GURU_PHOTO") ?: ""

        val tvNamaDepan    = findViewById<TextView>(R.id.tv_nama_depan)
        val tvNamaBelakang = findViewById<TextView>(R.id.tv_nama_belakang)
        val ivFoto         = findViewById<ImageView>(R.id.iv_wali_photo_detail)
        val btnKirimPesan  = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_kirim_pesan)

        // Pisah nama: bagian depan dan belakang untuk 2 TextView yang ada di layout
        val namaParts = guruNama.trim().split(" ")
        tvNamaDepan.text    = namaParts.firstOrNull() ?: guruNama
        tvNamaBelakang.text = if (namaParts.size > 1) namaParts.drop(1).joinToString(" ") else guruKelas

        // Load foto guru via Glide
        if (guruPhoto.isNotEmpty()) {
            Glide.with(this)
                .load(guruPhoto)
                .placeholder(R.drawable.image_3)
                .error(R.drawable.image_3)
                .into(ivFoto)
        } else {
            ivFoto.setImageResource(R.drawable.image_3)
        }

        // Tombol Kirim Pesan:
        // 1. Kirim nama wali kembali ke TerlambatActivity/DispensasiActivity/TidakHadirActivity
        // 2. Buka ChatActivity untuk chat langsung dengan guru
        btnKirimPesan.setOnClickListener {
            // Kirim hasil nama wali ke WaliKelasActivity (yang akan diteruskan ke form izin)
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("NAMA_WALI_TERPILIH", guruNama)
            })

            // Buka ChatActivity untuk chat langsung
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("OTHER_UID",  guruUid)
                putExtra("OTHER_NAMA", guruNama)
            })
        }
    }
}
