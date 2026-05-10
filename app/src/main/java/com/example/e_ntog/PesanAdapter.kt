package com.example.e_ntog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ChatItem(
    val chatId: String = "",
    val otherUid: String = "",
    val otherNama: String = "",
    val lastMessage: String = ""
)

class PesanAdapter(
    private val chatList: List<ChatItem>,
    private val onChatClick: (ChatItem) -> Unit
) : RecyclerView.Adapter<PesanAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tv_nama_lawan)
        val tvLastMessage: TextView = view.findViewById(R.id.tv_last_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daftar_pesan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position]
        holder.tvNama.text = chat.otherNama
        holder.tvLastMessage.text = chat.lastMessage
        holder.itemView.setOnClickListener { onChatClick(chat) }
    }

    override fun getItemCount() = chatList.size
}
