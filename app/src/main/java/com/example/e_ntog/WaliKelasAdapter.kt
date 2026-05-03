package com.example.e_ntog

import android.graphics.Color
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import de.hdodenhof.circleimageview.CircleImageView

data class WaliKelasModel(
    val uid: String = "",
    val nama: String = "",
    val namaKelas: String = "",
    val photoUrl: String = ""
)

class WaliKelasAdapter(
    private val list: List<WaliKelasModel>,
    private val onGuruClick: (WaliKelasModel) -> Unit
) : RecyclerView.Adapter<WaliKelasAdapter.ViewHolder>() {

    private val cardColors = listOf(
        "#4CAF50", "#3F51B5", "#E53935",
        "#1E88E5", "#FB8C00", "#8E24AA"
    )

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Hapus cardWali dan clCardBg
        val ivFoto: ImageView = itemView.findViewById(R.id.ivFotoWali)
        val tvNama: TextView  = itemView.findViewById(R.id.tvNamaWali)
        val tvKelas: TextView = itemView.findViewById(R.id.tvKelasWali)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wali_kelas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val guru = list[position]

        // Baris setBackgroundColor dihapus

        holder.tvNama.text  = guru.nama
        holder.tvKelas.text = guru.namaKelas.ifEmpty { "Belum memiliki kelas" }

        if (guru.photoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(guru.photoUrl)
                .placeholder(R.drawable.profile) // Aku ubah jadi icon profile bawaan
                .error(R.drawable.profile)
                .into(holder.ivFoto)
        } else {
            holder.ivFoto.setImageResource(R.drawable.profile)
        }

        // Pakai itemView untuk klik seluruh card
        holder.itemView.setOnClickListener { onGuruClick(guru) }
    }

    override fun getItemCount() = list.size
}
