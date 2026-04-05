package com.example.e_ntog

import android.os.Bundle
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
    private var kelasId = ""

    // Deklarasikan di level class agar bisa diakses di dalam lambda SnapshotListener
    private lateinit var rvForum: RecyclerView
    private lateinit var tvTitle: TextView

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

        // Ambil kelasId dari Firestore
        db.collection("users").document(session.getUid()).get()
            .addOnSuccessListener { doc ->
                kelasId = doc.getString("kelasId") ?: ""
                val kelasNama = doc.getString("kelasNama") ?: "Kelas"
                tvTitle.text = "Forum $kelasNama"

                if (kelasId.isNotEmpty()) {
                    loadForumMessages()
                } else {
                    Toast.makeText(this, "Kamu belum join kelas", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal load data: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        btnKirim.setOnClickListener {
            val text = etPesan.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            if (kelasId.isEmpty()) {
                Toast.makeText(this, "Belum join kelas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            etPesan.setText("")

            db.collection("kelas").document(kelasId)
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
    }

    private fun loadForumMessages() {
        db.collection("kelas").document(kelasId)
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
                if (messages.isNotEmpty()) {
                    rvForum.scrollToPosition(messages.size - 1)
                }
            }
    }
}
