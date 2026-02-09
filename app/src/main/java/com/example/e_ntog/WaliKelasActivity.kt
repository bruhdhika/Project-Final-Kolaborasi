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


        detailWaliLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val namaWali = result.data?.getStringExtra("NAMA_WALI_TERPILIH")


                val resultIntent = Intent()
                resultIntent.putExtra("NAMA_WALI_TERPILIH", namaWali)

                // Kirim hasilnya...
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }

        }


        // Temukan tombol back
        val backButton = findViewById<ImageView>(R.id.iv_back_arrow)
        backButton.setOnClickListener {
            finish()
        }

        // Temukan semua card guru
        val cardAgung = findViewById<ImageView>(R.id.card_Agung)
        val cardEko = findViewById<ImageView>(R.id.card_Eko)
        val cardAdi = findViewById<ImageView>(R.id.card_Adi)
        val cardJaya = findViewById<ImageView>(R.id.card_Jaya)

        // Beri listener (SEMUA MEMANGGIL FUNGSI 'pindahKeDetail')
        cardAgung.setOnClickListener {
            pindahKeDetail("Agung", "Kremes", R.drawable.kennan, "#F5F5F5")
        }
        cardEko.setOnClickListener {
            pindahKeDetail("Eko", "Lontong", R.drawable.andika, "#FFCA28")
        }
        cardAdi.setOnClickListener {
            pindahKeDetail("Adi", "Gemblong", R.drawable.moses, "#E53935")
        }
        cardJaya.setOnClickListener {
            pindahKeDetail("Jaya", "Gehu", R.drawable.rasya, "#1E88E5")
        }
    }

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