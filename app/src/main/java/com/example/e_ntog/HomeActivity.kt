package com.example.e_ntog // Pastikan nama package ini SAMA persis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // --- INI ADALAH KODE YANG SUDAH DIPERBAIKI ---

        // 1. Ambil SEMUA card berdasarkan ID-nya dari XML
        val cardTerlambat = findViewById<MaterialCardView>(R.id.card_terlambat)
        val cardIzinHadir = findViewById<MaterialCardView>(R.id.card_izin_hadir)
        val cardDispen = findViewById<MaterialCardView>(R.id.card_dispen)


        // 2. SAMBUNGAN YANG BENAR UNTUK CARD TERLAMBAT
        cardTerlambat.setOnClickListener {
            // Perintah: Pindah ke TerlambatActivity
            val intent = Intent(this@HomeActivity, TerlambatActivity::class.java)
            startActivity(intent)
        }

        // 3. SAMBUNGAN UNTUK CARD IZIN (biarkan kosong dulu)
        cardIzinHadir.setOnClickListener {
            // Nanti diisi kalau halamannya sudah dibuat
            // Contoh: val intent = Intent(this@HomeActivity, IzinHadirActivity::class.java)
            // startActivity(intent)
        }

        // 4. SAMBUNGAN UNTUK CARD DISPEN (ini yang salah di kodemu, sekarang dikosongkan)
        cardDispen.setOnClickListener {
            // KODE YANG SALAH SEBELUMNYA ADA DI SINI. SEKARANG KOSONG.
            // Nanti diisi kalau halamannya sudah dibuat
            // Contoh: val intent = Intent(this@HomeActivity, DispenActivity::class.java)
            // startActivity(intent)
        }
    }
}