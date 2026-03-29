package com.example.e_ntog

import android.os.Bundle
import android.text.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class WaliKelasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    // 2 list: satu untuk data asli, satu untuk yang ditampilkan (search filter)
    private val allGuruList      = mutableListOf<WaliKelasModel>()
    private val filteredGuruList = mutableListOf<WaliKelasModel>()
    private lateinit var adapter: WaliKelasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wali_kelas)

        val ivBack      = findViewById<ImageView>(R.id.iv_back_arrow)
        val etSearch    = findViewById<EditText>(R.id.et_search_wali)
        val progressBar = findViewById<ProgressBar>(R.id.pb_wali_loading)
        val rvWali      = findViewById<RecyclerView>(R.id.rv_wali_kelas)
        val tvEmpty     = findViewById<TextView>(R.id.tv_wali_empty)

        // Tombol back
        ivBack.setOnClickListener { finish() }

        // Setup RecyclerView — pakai filteredGuruList (yang berubah saat search)
        adapter = WaliKelasAdapter(filteredGuruList)
        rvWali.layoutManager = LinearLayoutManager(this)
        rvWali.adapter = adapter

        // Search filter — ketik → filter nama guru
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                filterGuru(keyword, rvWali, tvEmpty)
            }
        })

        // Load data guru dari Firestore
        loadGuruList(progressBar, rvWali, tvEmpty)
    }

    private fun loadGuruList(
        progressBar: ProgressBar,
        rvWali: RecyclerView,
        tvEmpty: TextView
    ) {
        progressBar.visibility = View.VISIBLE
        rvWali.visibility      = View.GONE
        tvEmpty.visibility     = View.GONE

        // Query: semua user dengan role = 'guru'
        db.collection("users")
            .whereEqualTo("role", "guru")
            .get()
            .addOnSuccessListener { snaps ->
                progressBar.visibility = View.GONE
                allGuruList.clear()

                snaps?.forEach { doc ->
                    // Ambil nama kelas pertama yang dimiliki guru (jika ada)
                    // namaKelas diambil dari subcollection 'kelas' secara terpisah
                    allGuruList.add(WaliKelasModel(
                        uid      = doc.id,
                        nama     = doc.getString("nama")     ?: "-",
                        photoUrl = doc.getString("photoUrl") ?: ""
                        // namaKelas diisi setelah query kelas di bawah
                    ))
                }

                // Setelah dapat list guru, ambil nama kelas tiap guru
                enrichWithKelasData(rvWali, tvEmpty)
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Gagal load data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Untuk setiap guru, cari kelas yang dia buat di collection 'kelas'
     * (query: guruUid == uid guru). Ambil namaKelas pertama yang ditemukan.
     * Setelah semua selesai, update filteredGuruList dan tampilkan RecyclerView.
     */
    private fun enrichWithKelasData(rvWali: RecyclerView, tvEmpty: TextView) {
        if (allGuruList.isEmpty()) {
            showResult(rvWali, tvEmpty)
            return
        }

        var completed = 0
        val total = allGuruList.size

        allGuruList.forEachIndexed { index, guru ->
            db.collection("kelas")
                .whereEqualTo("guruUid", guru.uid)
                .limit(1)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isEmpty) {
                        val namaKelas = task.result.documents[0].getString("namaKelas") ?: ""
                        // Update namaKelas di allGuruList
                        allGuruList[index] = guru.copy(namaKelas = namaKelas)
                    }
                    completed++
                    // Saat semua query kelas selesai → tampilkan
                    if (completed == total) {
                        showResult(rvWali, tvEmpty)
                    }
                }
        }
    }

    private fun showResult(rvWali: RecyclerView, tvEmpty: TextView) {
        filteredGuruList.clear()
        filteredGuruList.addAll(allGuruList)
        adapter.notifyDataSetChanged()

        if (filteredGuruList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvWali.visibility  = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvWali.visibility  = View.VISIBLE
        }
    }

    private fun filterGuru(keyword: String, rvWali: RecyclerView, tvEmpty: TextView) {
        filteredGuruList.clear()
        if (keyword.isEmpty()) {
            filteredGuruList.addAll(allGuruList)
        } else {
            filteredGuruList.addAll(
                allGuruList.filter { it.nama.contains(keyword, ignoreCase = true) }
            )
        }
        adapter.notifyDataSetChanged()

        if (filteredGuruList.isEmpty()) {
            tvEmpty.text       = "Wali kelas \"$keyword\" tidak ditemukan."
            tvEmpty.visibility = View.VISIBLE
            rvWali.visibility  = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvWali.visibility  = View.VISIBLE
        }
    }
}
