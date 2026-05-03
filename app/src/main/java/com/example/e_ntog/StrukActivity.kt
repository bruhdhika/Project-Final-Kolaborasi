package com.example.e_ntog

import android.Manifest // <-- IMPORT
import android.content.ContentValues // <-- IMPORT
import android.content.Intent
import android.content.pm.PackageManager // <-- IMPORT
import android.graphics.Bitmap // <-- IMPORT
import android.graphics.Canvas // <-- IMPORT
import android.net.Uri // <-- IMPORT
import android.os.Build // <-- IMPORT
import android.os.Bundle
import android.os.Environment // <-- IMPORT
import android.provider.MediaStore // <-- IMPORT
import android.view.View // <-- IMPORT
import android.widget.Button // <-- IMPORT
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast // <-- IMPORT
import androidx.activity.result.contract.ActivityResultContracts // <-- IMPORT
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat // <-- IMPORT
import com.google.android.material.card.MaterialCardView // <-- IMPORT
import java.io.OutputStream

class StrukActivity : BaseActivity() {

    // Launcher untuk minta izin
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Izin diberikan, silakan download lagi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin ditolak, tidak bisa download", Toast.LENGTH_SHORT).show()
            }
        }

    private lateinit var cardStruk: MaterialCardView // Definisikan card struk di sini

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_struk)
setupBackButton()
        // Ambil data dari Intent
        val nama = intent.getStringExtra("NAMA") ?: "Tidak Ada Data"
        val kelas = intent.getStringExtra("KELAS") ?: "Tidak Ada Data"
        val alasan = intent.getStringExtra("ALASAN") ?: "Tidak Ada Data"
        val waliKelas = intent.getStringExtra("WALI_KELAS") ?: "Tidak Ada Data"

        // Temukan komponen
        cardStruk = findViewById(R.id.card_struk) // Inisialisasi
        val tvNama = findViewById<TextView>(R.id.tv_struk_nama)
        val tvKelas = findViewById<TextView>(R.id.tv_struk_kelas)
        val tvAlasan = findViewById<TextView>(R.id.tv_struk_alasan)
        val tvWali = findViewById<TextView>(R.id.tv_struk_wali)
        val btnKembaliHome = findViewById<View>(R.id.btn_kembali_home)
        val btnDownload = findViewById<Button>(R.id.btn_download)
        val backButton = findViewById<ImageView>(R.id.iv_back_arrow)

        // Set data ke TextView
        tvNama.text = "Nama : $nama"
        tvKelas.text = "Kelas : $kelas"
        tvAlasan.text = "Alasan Terlambat : $alasan"
        tvWali.text = "Wali Kelas : $waliKelas"

        // --- Listener Tombol ---

        backButton.setOnClickListener {
            finish()
        }

        btnKembaliHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            // Hapus semua activity di atas HomeActivity
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        btnDownload.setOnClickListener {
            // Cek izin dulu
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // Android 9 ke bawah
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveStrukAsImage(cardStruk)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } else { // Android 10+
                // Tidak perlu izin khusus untuk menyimpan ke MediaStore
                saveStrukAsImage(cardStruk)
            }
        }
    }

    // --- Fungsi Download ---

    private fun saveStrukAsImage(view: View) {
        val bitmap = createBitmapFromView(view)

        val filename = "struk_izin_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/E-Zin")
            }
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        } else { // Android 9 ke bawah
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/E-Zin"
            val file = java.io.File(imagesDir)
            if (!file.exists()) {
                file.mkdirs()
            }
            val imageFile = java.io.File(imagesDir, filename)
            fos = java.io.FileOutputStream(imageFile)
            imageUri = Uri.fromFile(imageFile)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            Toast.makeText(this, "Struk disimpan di Galeri/Pictures/E-Zin", Toast.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(this, "Gagal menyimpan struk", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createBitmapFromView(view: View): Bitmap {
        // Ukur view-nya
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val totalHeight = view.measuredHeight

        // Buat bitmap
        val bitmap = Bitmap.createBitmap(view.width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}