package com.example.e_ntog

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Model Data
data class AnnouncementModel(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val guruNama: String = "",
    val kelasNama: String = "",
    val timestamp: Timestamp? = null
)

class AnnouncementActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private val annoList = mutableListOf<AnnouncementModel>()
    private lateinit var adapter: AnnouncementAdapter
    private val kelasMilikGuru = mutableListOf<KelasModel>()
    private var activeKelasId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement)

        // 1. PANGGIL BACK BUTTON DULU
        setupBackButton()

        session = SessionManager(this)

        val rvAnno   = findViewById<RecyclerView>(R.id.rv_announcement)
        val fabKirim = findViewById<View>(R.id.fab_kirim_announcement)
        val tvEmpty  = findViewById<TextView>(R.id.tv_anno_empty)
        val tvTitle  = findViewById<TextView?>(R.id.tv_anno_title_header)

        // 2. PASANG ADAPTER
        adapter = AnnouncementAdapter(annoList) { announcement ->
            showDetailAnnouncement(announcement)
        }

        rvAnno.layoutManager = LinearLayoutManager(this)
        rvAnno.adapter = adapter

        val isGuru = session.getRole() == SessionManager.ROLE_GURU

        if (isGuru) {
            fabKirim?.visibility = View.VISIBLE
            tvTitle?.text = "Pengumuman Saya"
            loadKelasGuru(tvEmpty)
            fabKirim?.setOnClickListener { showDialogPilihKelasDanKirim() }
        } else {
            fabKirim?.visibility = View.GONE
            tvTitle?.text = "Pengumuman"
            loadUserKelas(tvEmpty)
        }
    }

    private fun showDetailAnnouncement(anno: AnnouncementModel) {
        AlertDialog.Builder(this)
            .setTitle(anno.title)
            .setMessage("${anno.content}\n\nOleh: ${anno.guruNama}")
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun loadUserKelas(tvEmpty: TextView) {
        db.collection("users").document(session.getUid()).get()
            .addOnSuccessListener { doc ->
                activeKelasId = doc.getString("kelasId") ?: ""
                if (activeKelasId.isEmpty()) {
                    tvEmpty.text = "Kamu belum join kelas"
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    loadAnnouncements(activeKelasId, tvEmpty)
                }
            }
    }

    private fun loadKelasGuru(tvEmpty: TextView) {
        db.collection("kelas")
            .whereEqualTo("guruUid", session.getUid())
            .get()
            .addOnSuccessListener { snaps ->
                kelasMilikGuru.clear()
                snaps.forEach { doc ->
                    kelasMilikGuru.add(KelasModel(
                        kelasId   = doc.id,
                        namaKelas = doc.getString("namaKelas") ?: ""
                    ))
                }
                if (kelasMilikGuru.isNotEmpty()) {
                    activeKelasId = kelasMilikGuru[0].kelasId
                    loadAnnouncements(activeKelasId, tvEmpty)
                } else {
                    tvEmpty.visibility = View.VISIBLE
                }
            }
    }

    private fun loadAnnouncements(kelasId: String, tvEmpty: TextView) {
        db.collection("kelas").document(kelasId)
            .collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snaps, _ ->
                annoList.clear()
                snaps?.forEach { doc ->
                    annoList.add(AnnouncementModel(
                        id        = doc.id,
                        title     = doc.getString("title") ?: "",
                        content   = doc.getString("content") ?: "",
                        guruNama  = doc.getString("guruNama") ?: ""
                    ))
                }
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (annoList.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun showDialogPilihKelasDanKirim() {
        // Implementasi dialog kirim (skip detail biar gak kepanjangan)
    }
}