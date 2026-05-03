package com.example.e_ntog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
        val etSearch    = findViewById<EditText>(R.id.et_search_wali)
        val progressBar = findViewById<ProgressBar>(R.id.pb_wali_loading)
        val rvWali      = findViewById<RecyclerView>(R.id.rv_wali_kelas)
        val tvEmpty     = findViewById<TextView>(R.id.tv_wali_empty)

        ivBack.setOnClickListener { finish() }

        adapter = WaliKelasAdapter(filteredGuruList) { guru ->
            // 1. Kirim hasil nama wali ke Activity sebelumnya (Dispensasi/Terlambat/dll)
            val resultIntent = Intent()
            resultIntent.putExtra("NAMA_WALI_TERPILIH", guru.nama)
            setResult(Activity.RESULT_OK, resultIntent)

            // 2. Buka ChatActivity untuk chat langsung
            val chatIntent = Intent(this, ChatActivity::class.java).apply {
                putExtra("OTHER_UID", guru.uid)
                putExtra("OTHER_NAMA", guru.nama)
            }
            startActivity(chatIntent)

            // 3. Tutup halaman pilih wali kelas ini
            finish()
        }

        rvWali.layoutManager = LinearLayoutManager(this)
        rvWali.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterGuru(s.toString().trim(), rvWali, tvEmpty)
            }
        })

        loadGuruDariKelas(progressBar, rvWali, tvEmpty)
    }

    private fun loadGuruDariKelas(pb: ProgressBar, rv: RecyclerView, tvEmpty: TextView) {
        pb.visibility      = View.VISIBLE
        rv.visibility      = View.GONE
        tvEmpty.visibility = View.GONE

        val uid = session.getUid()

        // 1. Ambil data user (murid)
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val kelasId = userDoc.getString("kelasId") ?: ""
                if (kelasId.isEmpty()) {
                    showError(pb, tvEmpty, "Kamu belum join kelas.")
                    return@addOnSuccessListener
                }

                // 2. Ambil data kelas untuk mencari guruUid (admin kelas)
                db.collection("kelas").document(kelasId).get()
                    .addOnSuccessListener { kelasDoc ->
                        val guruUid   = kelasDoc.getString("guruUid")   ?: ""
                        val namaKelas = kelasDoc.getString("namaKelas") ?: "Kelas"

                        if (guruUid.isEmpty()) {
                            showError(pb, tvEmpty, "Wali kelas belum ditentukan.")
                            return@addOnSuccessListener
                        }

                        // 3. Ambil profil guru secara spesifik
                        db.collection("users").document(guruUid).get()
                            .addOnSuccessListener { guruDoc ->
                                pb.visibility = View.GONE
                                allGuruList.clear()

                                // Menambahkan hanya 1 wali kelas hasil filter sistem
                                allGuruList.add(WaliKelasModel(
                                    uid       = guruUid,
                                    nama      = guruDoc.getString("nama")     ?: "Guru",
                                    namaKelas = namaKelas,
                                    photoUrl  = guruDoc.getString("photoUrl") ?: ""
                                ))

                                filteredGuruList.clear()
                                filteredGuruList.addAll(allGuruList)
                                adapter.notifyDataSetChanged()
                                rv.visibility = View.VISIBLE
                            }
                            .addOnFailureListener {
                                showError(pb, tvEmpty, "Gagal memuat profil guru.")
                            }
                    }
                    .addOnFailureListener {
                        showError(pb, tvEmpty, "Gagal memuat data kelas.")
                    }
            }
            .addOnFailureListener {
                showError(pb, tvEmpty, "Gagal memuat data user.")
            }
    }

    private fun showError(pb: ProgressBar, tv: TextView, msg: String) {
        pb.visibility = View.GONE
        tv.text = msg
        tv.visibility = View.VISIBLE
    }


    private fun filterGuru(keyword: String, rv: RecyclerView, tvEmpty: TextView) {
        filteredGuruList.clear()
        filteredGuruList.addAll(
            if (keyword.isEmpty()) allGuruList
            else allGuruList.filter { it.nama.contains(keyword, ignoreCase = true) }
        )
        adapter.notifyDataSetChanged()
        tvEmpty.visibility = if (filteredGuruList.isEmpty()) View.VISIBLE else View.GONE
        rv.visibility      = if (filteredGuruList.isEmpty()) View.GONE   else View.VISIBLE
    }
}