package com.example.e_ntog

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ForumKelasActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private var activeKelasId = ""

    private lateinit var rvForum: RecyclerView
    private lateinit var tvTitle: TextView

    // Untuk guru: list kelas miliknya
    private val kelasMilikGuru = mutableListOf<KelasModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum_kelas)
        setupBackButton()

        session = SessionManager(this)

        rvForum  = findViewById(R.id.rv_forum)
        tvTitle  = findViewById(R.id.tv_forum_title)
        val etPesan  = findViewById<EditText>(R.id.et_forum_pesan)
        val btnKirim = findViewById<ImageView>(R.id.iv_forum_kirim)
        val ivBack   = findViewById<ImageView>(R.id.iv_back_forum)

        ivBack.setOnClickListener { finish() }

        adapter = ChatAdapter(messages, session.getUid())
        rvForum.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvForum.adapter = adapter

        val isGuru = session.getRole() == SessionManager.ROLE_GURU

        if (isGuru) {
            // ── GURU FLOW ──────────────────────────────────────────────────
            // Guru lihat forum kelas yang dia pilih (dari kelas yang dia buat)
            loadKelasMilikGuru()
        } else {
            // ── MURID FLOW ─────────────────────────────────────────────────
            db.collection("users").document(session.getUid()).get()
                .addOnSuccessListener { doc ->
                    activeKelasId = doc.getString("kelasId") ?: ""
                    val kelasNama = doc.getString("kelasNama") ?: "Kelas"
                    tvTitle.text = "Forum $kelasNama"
                    if (activeKelasId.isNotEmpty()) {
                        loadForumMessages()
                    } else {
                        Toast.makeText(this, "Kamu belum join kelas", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal load data: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnKirim.setOnClickListener {
            val text = etPesan.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            if (activeKelasId.isEmpty()) {
                Toast.makeText(this, "Pilih kelas terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            etPesan.setText("")
            kirimPesan(text)
        }
    }

    // ── Guru: load kelas miliknya ─────────────────────────────────────────
    private fun loadKelasMilikGuru() {
        db.collection("kelas")
            .whereEqualTo("guruUid", session.getUid())
            .get()
            .addOnSuccessListener { snaps ->
                kelasMilikGuru.clear()
                snaps.forEach { doc ->
                    kelasMilikGuru.add(KelasModel(
                        kelasId   = doc.id,
                        namaKelas = doc.getString("namaKelas") ?: "",
                        kodeKelas = doc.getString("kodeKelas") ?: ""
                    ))
                }
                if (kelasMilikGuru.isEmpty()) {
                    tvTitle.text = "Forum Kelas"
                    Toast.makeText(this, "Kamu belum membuat kelas", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                // Tampilkan kelas pertama secara default
                activeKelasId = kelasMilikGuru[0].kelasId
                tvTitle.text  = "Forum ${kelasMilikGuru[0].namaKelas}"
                loadForumMessages()
                // Tampilkan selector kelas
                showKelasSelector()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal load kelas: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Chip selector kelas (hanya untuk guru)
    private fun showKelasSelector() {
        val tvSelector = findViewById<TextView?>(R.id.tv_forum_kelas_selector) ?: return
        tvSelector.visibility = View.VISIBLE
        tvSelector.text = kelasMilikGuru.find { it.kelasId == activeKelasId }?.namaKelas?.let { "Kelas: $it ▼" } ?: "Pilih Kelas ▼"
        tvSelector.setOnClickListener {
            val namaArr = kelasMilikGuru.map { it.namaKelas }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Pilih Kelas")
                .setItems(namaArr) { _, which ->
                    val kelas = kelasMilikGuru[which]
                    activeKelasId = kelas.kelasId
                    tvTitle.text  = "Forum ${kelas.namaKelas}"
                    tvSelector.text = "Kelas: ${kelas.namaKelas} ▼"
                    messages.clear()
                    adapter.notifyDataSetChanged()
                    loadForumMessages()
                }
                .show()
        }
    }

    // ── Load pesan forum ──────────────────────────────────────────────────
    private fun loadForumMessages() {
        db.collection("kelas").document(activeKelasId)
            .collection("forum")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snaps, error ->
                if (error != null) return@addSnapshotListener
                messages.clear()
                snaps?.forEach { doc ->
                    messages.add(ChatMessage(
                        senderId   = doc.getString("senderUid")  ?: "",
                        senderNama = doc.getString("senderNama") ?: "",
                        text       = doc.getString("text")       ?: ""
                    ))
                }
                adapter.notifyDataSetChanged()
                if (messages.isNotEmpty()) rvForum.scrollToPosition(messages.size - 1)
            }
    }

    // ── Kirim pesan ke forum ──────────────────────────────────────────────
    private fun kirimPesan(text: String) {
        db.collection("kelas").document(activeKelasId)
            .collection("forum")
            .add(hashMapOf(
                "senderUid"  to session.getUid(),
                "senderNama" to session.getNama(),
                "text"       to text,
                "timestamp"  to FieldValue.serverTimestamp()
            ))
            .addOnFailureListener {
                Toast.makeText(this, "Gagal kirim: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Kirim pesan sistem ke forum kelas (dipanggil dari luar, misal setelah submit izin).
     * Contoh: "Sistem: Andi mengajukan izin terlambat pada 01 Jan 2025"
     */
    companion object {
        fun kirimPesanSistem(
            db: FirebaseFirestore,
            kelasId: String,
            pesanSistem: String
        ) {
            if (kelasId.isEmpty()) return
            db.collection("kelas").document(kelasId)
                .collection("forum")
                .add(hashMapOf(
                    "senderUid"  to "SISTEM",
                    "senderNama" to "📢 Sistem",
                    "text"       to pesanSistem,
                    "timestamp"  to FieldValue.serverTimestamp()
                ))
        }
    }
}
