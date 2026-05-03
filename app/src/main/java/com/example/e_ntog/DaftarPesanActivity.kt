package com.example.e_ntog

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DaftarPesanActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_pesan)

        // Tombol Back
        findViewById<ImageView>(R.id.iv_back_pesan).setOnClickListener {
            finish()
        }

        val rvPesan = findViewById<RecyclerView>(R.id.rv_daftar_pesan)
        val tvEmpty = findViewById<TextView>(R.id.tv_pesan_empty)

        // Nanti di sini kita pasang logika untuk ngambil data chat dari Firebase.
        // Sementara kita tampilkan teks "Belum ada pesan" dulu biar rapi.
        rvPesan.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
    }
}