package com.example.e_ntog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class EditDataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data)

        val etNama = findViewById<EditText>(R.id.et_edit_nama)
        val etPass = findViewById<EditText>(R.id.et_edit_password)
        val etConfirm = findViewById<EditText>(R.id.et_confirm_password)
        val layoutConfirm = findViewById<LinearLayout>(R.id.layout_konfirmasi_pass)
        val btnSimpan = findViewById<Button>(R.id.btn_simpan_perubahan)

        // Konfirmasi password muncul otomatis
        etPass.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                layoutConfirm.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSimpan.setOnClickListener {
            val intentResult = Intent()
            intentResult.putExtra("NAMA_BARU", etNama.text.toString())
            setResult(Activity.RESULT_OK, intentResult)
            finish()
        }
    }
}