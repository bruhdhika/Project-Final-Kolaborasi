package com.example.e_ntog

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import java.io.OutputStream

class StrukTidakHadir : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Izin diberikan, silakan download lagi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin ditolak, tidak bisa download", Toast.LENGTH_SHORT).show()
            }
        }

    private lateinit var cardStruk: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_struk_tidak_hadir)

        // Ambil data
        val nama = intent.getStringExtra("NAMA") ?: "Tidak Ada Data"
        val kelas = intent.getStringExtra("KELAS") ?: "Tidak Ada Data"
        val alasan = intent.getStringExtra("ALASAN") ?: "Tidak Ada Data"
        val waliKelas = intent.getStringExtra("WALI_KELAS") ?: "Tidak Ada Data"

        // Temukan komponen
        cardStruk = findViewById(R.id.card_struk)
        val tvNama = findViewById<TextView>(R.id.tv_struk_nama)
        val tvKelas = findViewById<TextView>(R.id.tv_struk_kelas)
        val tvAlasan = findViewById<TextView>(R.id.tv_struk_alasan)
        val tvWali = findViewById<TextView>(R.id.tv_struk_wali)
        val btnKembaliHome = findViewById<AppCompatButton>(R.id.btn_kembali_home)
        val btnDownload = findViewById<Button>(R.id.btn_download)
        val backButton = findViewById<ImageView>(R.id.iv_back_arrow)

        // Set text
        tvNama.text = "Nama : $nama"
        tvKelas.text = "Kelas : $kelas"
        tvAlasan.text = "Alasan Tidak Masuk : $alasan"
        tvWali.text = "Wali Kelas : $waliKelas"

        // Tombol back
        backButton.setOnClickListener {
            finish()
        }

        // Tombol balik ke home
        btnKembaliHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Tombol download
        btnDownload.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    saveStrukAsImage(cardStruk)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } else {
                saveStrukAsImage(cardStruk)
            }
        }
    }

    // Simpen gambar
    private fun saveStrukAsImage(view: View) {
        val bitmap = createBitmapFromView(view)

        val filename = "struk_tidak_hadir_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/E-Zin")
            }
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/E-Zin"
            val file = java.io.File(imagesDir)
            if (!file.exists()) file.mkdirs()

            val imageFile = java.io.File(imagesDir, filename)
            fos = java.io.FileOutputStream(imageFile)
            imageUri = Uri.fromFile(imageFile)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            Toast.makeText(this, "Struk Tidak Hadir disimpan di Galeri/Pictures/E-Zin", Toast.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(this, "Gagal menyimpan struk", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createBitmapFromView(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val totalHeight = view.measuredHeight

        val bitmap = Bitmap.createBitmap(view.width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}
