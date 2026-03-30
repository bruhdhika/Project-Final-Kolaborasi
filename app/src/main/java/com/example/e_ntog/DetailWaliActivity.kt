package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class DetailWaliActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_wali)
        setupBackButton()

        // Terima data dinamis dari WaliKelasActivity
        val guruUid    = intent.getStringExtra("GURU_UID")   ?: ""
        val guruNama   = intent.getStringExtra("GURU_NAMA")  ?: "-"
        val guruKelas  = intent.getStringExtra("GURU_KELAS") ?: "-"
        val guruPhoto  = intent.getStringExtra("GURU_PHOTO") ?: ""

        val tvNamaDepan    = findViewById<TextView>(R.id.tv_nama_depan)
        val tvNamaBelakang = findViewById<TextView>(R.id.tv_nama_belakang)
        val ivFoto         = findViewById<ImageView>(R.id.iv_wali_photo_detail)
        val btnKirimPesan  = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_kirim_pesan)

        // Pisah nama jadi 2 bagian (depan & belakang) untuk layout yang sudah ada
        val namaParts = guruNama.split(" ")
        tvNamaDepan.text    = namaParts.firstOrNull() ?: guruNama
        tvNamaBelakang.text = if (namaParts.size > 1) namaParts.drop(1).joinToString(" ") else guruKelas

        // Load foto guru
        if (guruPhoto.isNotEmpty()) {
            Glide.with(this).load(guruPhoto).placeholder(R.drawable.image_3).into(ivFoto)
        } else {
            ivFoto.setImageResource(R.drawable.image_3)
        }

        // Tombol Kirim Pesan → buka ChatActivity
        btnKirimPesan.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("OTHER_UID",  guruUid)
            intent.putExtra("OTHER_NAMA", guruNama)
            startActivity(intent)
        }
    }
}