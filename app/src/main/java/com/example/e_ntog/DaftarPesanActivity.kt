package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DaftarPesanActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private val chatList = mutableListOf<ChatItem>()
    private lateinit var adapter: PesanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_pesan)

        session = SessionManager(this)

        findViewById<ImageView>(R.id.iv_back_pesan).setOnClickListener { finish() }

        val rvPesan = findViewById<RecyclerView>(R.id.rv_daftar_pesan)
        val tvEmpty = findViewById<TextView>(R.id.tv_pesan_empty)

        adapter = PesanAdapter(chatList) { chat ->
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("OTHER_UID", chat.otherUid)
                putExtra("OTHER_NAMA", chat.otherNama)
            })
        }
        rvPesan.layoutManager = LinearLayoutManager(this)
        rvPesan.adapter = adapter

        loadPesanList(rvPesan, tvEmpty)
    }

    private fun loadPesanList(rvPesan: RecyclerView, tvEmpty: TextView) {
        val myUid = session.getUid()
        db.collection("chats")
            .whereArrayContains("participants", myUid)
            .addSnapshotListener { snaps, _ ->
                chatList.clear()
                snaps?.documents?.forEach { doc ->
                    val participants = doc.get("participants") as? List<String> ?: emptyList()
                    val otherUid = participants.firstOrNull { it != myUid } ?: ""
                    
                    if (otherUid.isNotEmpty()) {
                        db.collection("users").document(otherUid).get().addOnSuccessListener { userDoc ->
                            val otherNama = userDoc.getString("nama") ?: "Pengguna"
                            
                            doc.reference.collection("messages")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { msgSnaps ->
                                    val lastMsg = msgSnaps.documents.firstOrNull()?.getString("text") ?: ""
                                    chatList.add(ChatItem(doc.id, otherUid, otherNama, lastMsg))
                                    adapter.notifyDataSetChanged()
                                    
                                    tvEmpty.visibility = if (chatList.isEmpty()) View.VISIBLE else View.GONE
                                    rvPesan.visibility = if (chatList.isEmpty()) View.GONE else View.VISIBLE
                                }
                        }
                    }
                }
            }
    }
}
