package com.example.e_ntog

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ForumKelasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private var kelasId = ""

    // 1. PINDAHKAN VARIABEL KE SINI AGAR BISA DIAKSES SEMUA FUNGSI
    private lateinit var rvForum: RecyclerView
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum_kelas)

        session = SessionManager(this)

        // 2. INISIALISASI VARIABEL TANPA KATA 'val'
        rvForum   = findViewById(R.id.rv_forum)
        tvTitle   = findViewById(R.id.tv_forum_title)

        val etPesan   = findViewById<EditText>(R.id.et_forum_pesan)
        val btnKirim  = findViewById<ImageView>(R.id.iv_forum_kirim)
        val ivBack    = findViewById<ImageView>(R.id.iv_back_forum)

        ivBack.setOnClickListener { finish() }

        adapter = ChatAdapter(messages, session.getUid())
        rvForum.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvForum.adapter = adapter

        // Ambil kelasId
        db.collection("users").document(session.getUid()).get()
            .addOnSuccessListener { doc ->
                kelasId = doc.getString("kelasId") ?: ""
                val kelasNama = doc.getString("kelasNama") ?: "Kelas"
                tvTitle.text = "Forum $kelasNama"
                if (kelasId.isNotEmpty()) loadForumMessages()
            }

        btnKirim.setOnClickListener {
            val text = etPesan.text.toString().trim()
            if (text.isEmpty() || kelasId.isEmpty()) return@setOnClickListener
            etPesan.setText("")
            db.collection("kelas").document(kelasId)
                .collection("forum")
                .add(hashMapOf(
                    "senderUid"   to session.getUid(),
                    "senderNama"  to session.getNama(),
                    "text"        to text,
                    "timestamp"   to FieldValue.serverTimestamp()
                ))
        }
    }

    private fun loadForumMessages() {
        db.collection("kelas").document(kelasId)
            .collection("forum")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snaps, _ ->
                messages.clear()
                snaps?.forEach { doc ->
                    messages.add(ChatMessage(
                        senderId   = doc.getString("senderUid")  ?: "",
                        senderNama = doc.getString("senderNama") ?: "",
                        text       = doc.getString("text")       ?: ""
                    ))
                }
                adapter.notifyDataSetChanged()

                // SEKARANG rvForum SUDAH BISA TERBACA DI SINI
                if (messages.isNotEmpty()) {
                    rvForum.scrollToPosition(messages.size - 1)
                }
            }
    }
}