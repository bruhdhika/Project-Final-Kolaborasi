package com.example.e_ntog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvNama: TextView

    private val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val nama = result.data?.getStringExtra("NAMA_BARU")
            if (!nama.isNullOrEmpty()) tvNama.text = nama
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvNama = findViewById(R.id.namapengguna)
        val btnEdit = findViewById<ImageButton>(R.id.btn_edit)
        val btnBack = findViewById<ImageButton>(R.id.btn_back)

        btnEdit.setOnClickListener {
            editLauncher.launch(Intent(this, EditDataActivity::class.java))
        }

        btnBack.setOnClickListener { finish() }
    }
}