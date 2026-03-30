package com.example.e_ntog

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnnouncementAdapter(private val list: List<AnnouncementModel>)
    : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle:   TextView = view.findViewById(R.id.tv_anno_title)
        val tvContent: TextView = view.findViewById(R.id.tv_anno_content)
        val tvGuru:    TextView = view.findViewById(R.id.tv_anno_guru)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_announcement, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val anno = list[position]
        holder.tvTitle.text   = anno.title
        holder.tvContent.text = anno.content
        holder.tvGuru.text    = "dari: ${anno.guruNama}"
    }

    override fun getItemCount() = list.size
}
