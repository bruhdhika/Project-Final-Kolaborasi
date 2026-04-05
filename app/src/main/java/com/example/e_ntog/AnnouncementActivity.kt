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

data class AnnouncementModel(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val guruNama: String = "",
    val timestamp: Timestamp? = null
)

class AnnouncementActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private val annoList = mutableListOf<AnnouncementModel>()
    private lateinit var adapter: AnnouncementAdapter
    private var kelasId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement)
        setupBackButton()

        session = SessionManager(this)

        val rvAnno   = findViewById<RecyclerView>(R.id.rv_announcement)
        val fabKirim = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fab_kirim_announcement)
        val ivBack   = findViewById<ImageView>(R.id.iv_back_anno)
        val tvEmpty  = findViewById<TextView>(R.id.tv_anno_empty)

        ivBack.setOnClickListener { finish() }

        // FAB hanya muncul untuk guru
        fabKirim.visibility = if (session.getRole() == SessionManager.ROLE_GURU)
            View.VISIBLE else View.GONE

        adapter = AnnouncementAdapter(annoList)
        rvAnno.layoutManager = LinearLayoutManager(this)
        rvAnno.adapter = adapter

        // Ambil kelasId dari Firestore
        db.collection("users").document(session.getUid()).get()
            .addOnSuccessListener { doc ->
                kelasId = doc.getString("kelasId") ?: ""
                if (kelasId.isEmpty()) {
                    tvEmpty.text       = "Belum join kelas"
                    tvEmpty.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }
                loadAnnouncements(tvEmpty)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal load data: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        fabKirim.setOnClickListener { showDialogKirimAnnouncement() }
    }

    private fun loadAnnouncements(tvEmpty: TextView) {
        db.collection("kelas").document(kelasId)
            .collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snaps, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                annoList.clear()
                snaps?.forEach { doc ->
                    annoList.add(AnnouncementModel(
                        id        = doc.id,
                        title     = doc.getString("title")    ?: "",
                        content   = doc.getString("content")  ?: "",
                        guruNama  = doc.getString("guruNama") ?: "",
                        timestamp = doc.getTimestamp("timestamp")
                    ))
                }
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (annoList.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun showDialogKirimAnnouncement() {
        if (kelasId.isEmpty()) {
            Toast.makeText(this, "Belum join kelas", Toast.LENGTH_SHORT).show()
            return
        }

        val view      = layoutInflater.inflate(R.layout.dialog_kirim_announcement, null)
        val etTitle   = view.findViewById<EditText>(R.id.et_anno_title)
        val etContent = view.findViewById<EditText>(R.id.et_anno_content)

        AlertDialog.Builder(this)
            .setTitle("Kirim Pengumuman")
            .setView(view)
            .setPositiveButton("Kirim") { _, _ ->
                val title   = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()
                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(this, "Judul dan isi wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                db.collection("kelas").document(kelasId)
                    .collection("announcements")
                    .add(hashMapOf(
                        "title"     to title,
                        "content"   to content,
                        "guruNama"  to session.getNama(),
                        "guruUid"   to session.getUid(),
                        "timestamp" to FieldValue.serverTimestamp()
                    ))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pengumuman terkirim!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
