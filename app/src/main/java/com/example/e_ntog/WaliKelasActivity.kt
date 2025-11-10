package com.example.e_ntog

import android.annotation.SuppressLint
import android.app.Activity // <-- IMPORT INI
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher // <-- IMPORT INI
import androidx.activity.result.contract.ActivityResultContracts // <-- IMPORT INI
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class WaliKelasActivity : AppCompatActivity() {

    // Definisikan 'Launcher' untuk menerima hasil
    private lateinit var detailWaliLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wali_kelas)

        // --- INI ADALAH BAGIAN BARU ---
        // Inisialisasi Launcher.
        // Kode di dalam { ... } ini akan dieksekusi saat DetailWaliActivity ditutup
        detailWaliLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Cek apakah hasilnya "OK" (berarti tombol "Kirim Pesan" diklik)
            if (result.resultCode == Activity.RESULT_OK) {
                // Ambil data nama wali dari hasil
                val namaWali = result.data?.getStringExtra("NAMA_WALI_TERPILIH")

                // Buat Intent HASIL BARU untuk dikirim balik ke TerlambatActivity
                val resultIntent = Intent()
                resultIntent.putExtra("NAMA_WALI_TERPILIH", namaWali)

                // Kirim hasilnya...
                setResult(Activity.RESULT_OK, resultIntent)

                // ...dan tutup halaman daftar wali ini
                finish()
            }
            // Jika hasilnya BUKAN OK (misal, user tekan back), kita tidak lakukan apa-apa
        }
        // --- Akhir bagian baru ---

        // Temukan tombol back
        val backButton = findViewById<ImageView>(R.id.iv_back_arrow)
        backButton.setOnClickListener {
            finish()
        }

        // Temukan semua card guru
        val cardBambang = findViewById<MaterialCardView>(R.id.card_bambang)
        val cardKomarudin = findViewById<MaterialCardView>(R.id.card_komarudin)
        val cardNeneng = findViewById<MaterialCardView>(R.id.card_neneng)
        val cardAmelia = findViewById<MaterialCardView>(R.id.card_amelia)

        // Beri listener (SEMUA MEMANGGIL FUNGSI 'pindahKeDetail')
        cardBambang.setOnClickListener {
            pindahKeDetail("Bambang Sugi", "Asmaradun", R.drawable.kennan, "#F5F5F5")
        }
        cardKomarudin.setOnClickListener {
            pindahKeDetail("Komarudin", "Wijaya", R.drawable.oranglaki, "#FFCA28")
        }
        cardNeneng.setOnClickListener {
            pindahKeDetail("Neneng", "Kumala", R.drawable.kennan, "#E53935")
        }
        cardAmelia.setOnClickListener {
            pindahKeDetail("Amelia", "Rianty", R.drawable.oranglaki, "#1E88E5")
        }
    }

    // --- REVISI FUNGSI INI ---
    private fun pindahKeDetail(namaDepan: String, namaBelakang: String, fotoResId: Int, warnaBg: String) {
        val intent = Intent(this, DetailWaliActivity::class.java)
        intent.putExtra("NAMA_DEPAN", namaDepan)
        intent.putExtra("NAMA_BELAKANG", namaBelakang)
        intent.putExtra("FOTO_ID", fotoResId)
        intent.putExtra("WARNA_BG", warnaBg)

        // JANGAN 'startActivity', TAPI 'launch' DENGAN LAUNCHER KITA
        detailWaliLauncher.launch(intent)
    }
}