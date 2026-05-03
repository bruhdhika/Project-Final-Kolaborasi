package com.example.e_ntog

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnnouncementAdapter(
    private val list: List<AnnouncementModel>,
    private val onItemClick: (AnnouncementModel) -> Unit
) : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle:   TextView = view.findViewById(R.id.tv_anno_title)
        val tvContent: TextView = view.findViewById(R.id.tv_anno_content)
        val tvGuru:    TextView = view.findViewById(R.id.tv_anno_guru)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_announcement, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val anno = list[position]
        holder.apply {
            tvTitle.text   = anno.title
            tvContent.text = anno.content
            tvGuru.text    = anno.guruNama

            // Satu item bisa diklik
            itemView.setOnClickListener { onItemClick(anno) }
        }
    }

    override fun getItemCount() = list.size
}