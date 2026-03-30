package com.example.e_ntog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class WaliKelasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val allGuruList      = mutableListOf<WaliKelasModel>()
    private val filteredGuruList = mutableListOf<WaliKelasModel>()
    private lateinit var adapter: WaliKelasAdapter
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wali_kelas)

        session = SessionManager(this)

        val ivBack      = findViewById<ImageView>(R.id.iv_back_arrow)
        val etSearch    = findViewById<android.widget.EditText>(R.id.et_search_wali)
        val progressBar = findViewById<ProgressBar>(R.id.pb_wali_loading)
        val rvWali      = findViewById<RecyclerView>(R.id.rv_wali_kelas)
        val tvEmpty     = findViewById<TextView>(R.id.tv_wali_empty)

        ivBack.setOnClickListener { finish() }

        // Adapter: klik guru → DetailWaliActivity dengan data dinamis
        adapter = WaliKelasAdapter(filteredGuruList) { guru ->
            val intent = Intent(this, DetailWaliActivity::class.java)
            intent.putExtra("GURU_UID",    guru.uid)
            intent.putExtra("GURU_NAMA",   guru.nama)
            intent.putExtra("GURU_KELAS",  guru.namaKelas)
            intent.putExtra("GURU_PHOTO",  guru.photoUrl)
            startActivityForResult(intent, 100)
        }
        rvWali.layoutManager = LinearLayoutManager(this)
        rvWali.adapter = adapter

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterGuru(s.toString().trim(), rvWali, tvEmpty)
            }
        })

        loadGuruDariKelasMusid(progressBar, rvWali, tvEmpty)
    }

    /**
     * Load HANYA guru yang mengelola kelas murid ini.
     * Alur: users/{uid}.kelasId → kelas/{kelasId}.guruUid → users/{guruUid}
     */
    private fun loadGuruDariKelasMusid(pb: ProgressBar, rv: RecyclerView, tvEmpty: TextView) {
        pb.visibility  = View.VISIBLE
        rv.visibility  = View.GONE
        tvEmpty.visibility = View.GONE

        val uid = session.getUid()

        // Step 1: ambil kelasId murid
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val kelasId = userDoc.getString("kelasId") ?: ""
                if (kelasId.isEmpty()) {
                    pb.visibility = View.GONE
                    tvEmpty.text = "Kamu belum join kelas. Join kelas dulu!"
                    tvEmpty.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                // Step 2: ambil guruUid dari kelas
                db.collection("kelas").document(kelasId).get()
                    .addOnSuccessListener { kelasDoc ->
                        val guruUid   = kelasDoc.getString("guruUid")   ?: ""
                        val namaKelas = kelasDoc.getString("namaKelas") ?: ""
                        if (guruUid.isEmpty()) {
                            pb.visibility = View.GONE
                            tvEmpty.text = "Guru kelas belum terdaftar."
                            tvEmpty.visibility = View.VISIBLE
                            return@addOnSuccessListener
                        }

                        // Step 3: ambil profil guru
                        db.collection("users").document(guruUid).get()
                            .addOnSuccessListener { guruDoc ->
                                pb.visibility = View.GONE
                                allGuruList.clear()
                                allGuruList.add(WaliKelasModel(
                                    uid      = guruUid,
                                    nama     = guruDoc.getString("nama")     ?: "-",
                                    namaKelas= namaKelas,
                                    photoUrl = guruDoc.getString("photoUrl") ?: ""
                                ))
                                filteredGuruList.clear()
                                filteredGuruList.addAll(allGuruList)
                                adapter.notifyDataSetChanged()
                                rv.visibility = View.VISIBLE
                            }
                            .addOnFailureListener {
                                pb.visibility = View.GONE
                                tvEmpty.text = "Gagal load guru: ${it.message}"
                                tvEmpty.visibility = View.VISIBLE
                            }
                    }
            }
            .addOnFailureListener {
                pb.visibility = View.GONE
                tvEmpty.text = "Gagal: ${it.message}"
                tvEmpty.visibility = View.VISIBLE
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val namaWali = data?.getStringExtra("NAMA_WALI_TERPILIH") ?: ""
            // Kirim balik ke pemanggil (TerlambatActivity, dll)
            val result = Intent()
            result.putExtra("NAMA_WALI_TERPILIH", namaWali)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    private fun filterGuru(keyword: String, rv: RecyclerView, tvEmpty: TextView) {
        filteredGuruList.clear()
        filteredGuruList.addAll(
            if (keyword.isEmpty()) allGuruList
            else allGuruList.filter { it.nama.contains(keyword, ignoreCase = true) }
        )
        adapter.notifyDataSetChanged()
        tvEmpty.visibility = if (filteredGuruList.isEmpty()) View.VISIBLE else View.GONE
        rv.visibility      = if (filteredGuruList.isEmpty()) View.GONE else View.VISIBLE
    }
}
