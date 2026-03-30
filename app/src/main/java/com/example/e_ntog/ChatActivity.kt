package com.example.e_ntog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class ChatMessage(
    val senderId: String = "",
    val senderNama: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)

class ChatActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private var chatId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        session = SessionManager(this)

        val otherUid  = intent.getStringExtra("OTHER_UID")  ?: ""
        val otherNama = intent.getStringExtra("OTHER_NAMA") ?: "Guru"

        val tvTitle   = findViewById<TextView>(R.id.tv_chat_title)
        val rvChat    = findViewById<RecyclerView>(R.id.rv_chat)
        val etPesan   = findViewById<EditText>(R.id.et_pesan_chat)
        val btnKirim  = findViewById<ImageView>(R.id.iv_btn_kirim)
        val ivBack    = findViewById<ImageView>(R.id.iv_back_chat)

        tvTitle.text = otherNama
        ivBack.setOnClickListener { finish() }

        // Chat ID = gabungan 2 UID diurutkan secara leksikografis agar unik
        val myUid = session.getUid()
        chatId = if (myUid < otherUid) "${myUid}_${otherUid}" else "${otherUid}_${myUid}"

        // Pastikan dokumen chat ada
        db.collection("chats").document(chatId)
            .set(mapOf("participants" to listOf(myUid, otherUid)),
                com.google.firebase.firestore.SetOptions.merge())

        // Setup RecyclerView
        adapter = ChatAdapter(messages, myUid)
        rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvChat.adapter = adapter

        // Listen pesan realtime
        db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snaps, _ ->
                messages.clear()
                snaps?.forEach { doc ->
                    messages.add(ChatMessage(
                        senderId   = doc.getString("senderId")   ?: "",
                        senderNama = doc.getString("senderNama") ?: "",
                        text       = doc.getString("text")       ?: "",
                        timestamp  = doc.getTimestamp("timestamp")
                    ))
                }
                adapter.notifyDataSetChanged()
                if (messages.isNotEmpty()) rvChat.scrollToPosition(messages.size - 1)
            }

        // Kirim pesan
        btnKirim.setOnClickListener {
            val text = etPesan.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            etPesan.setText("")
            val msg = hashMapOf(
                "senderId"   to myUid,
                "senderNama" to session.getNama(),
                "text"       to text,
                "timestamp"  to FieldValue.serverTimestamp()
            )
            db.collection("chats").document(chatId).collection("messages").add(msg)
        }
    }
}
