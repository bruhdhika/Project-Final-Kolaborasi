package com.example.e_ntog

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val list: List<ChatMessage>,
    private val myUid: String
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    companion object {
        const val VIEW_SENT = 1
        const val VIEW_RECV = 2
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvText:   TextView = view.findViewById(R.id.tv_chat_text)
        val tvSender: TextView = view.findViewById(R.id.tv_chat_sender)
    }

    override fun getItemViewType(position: Int) =
        if (list[position].senderId == myUid) VIEW_SENT else VIEW_RECV

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == VIEW_SENT)
            R.layout.item_chat_sent else R.layout.item_chat_recv
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = list[position]
        holder.tvText.text   = msg.text
        holder.tvSender.text = msg.senderNama
    }

    override fun getItemCount() = list.size
}
