package com.example.e_ntog

import android.graphics.Color
import android.view.*
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
    private val list: List<WaliKelasModel>
) : RecyclerView.Adapter<WaliKelasAdapter.ViewHolder>() {

    // Warna card bergantian — meniru desain lama
    private val cardColors = listOf(
        "#4CAF50", "#3F51B5", "#E53935",
        "#1E88E5", "#FB8C00", "#8E24AA"
    )

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardWali:  MaterialCardView  = view.findViewById(R.id.card_wali)
        val clCardBg:  ConstraintLayout  = view.findViewById(R.id.cl_card_bg)
        val ivFoto:    CircleImageView   = view.findViewById(R.id.iv_guru_foto)
        val tvNama:    TextView          = view.findViewById(R.id.tv_guru_nama)
        val tvKelas:   TextView          = view.findViewById(R.id.tv_guru_kelas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wali_kelas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val guru = list[position]

        // Warna card acak berdasarkan posisi
        val warna = cardColors[position % cardColors.size]
        holder.clCardBg.setBackgroundColor(Color.parseColor(warna))

        holder.tvNama.text  = guru.nama
        holder.tvKelas.text = guru.namaKelas.ifEmpty { "Belum memiliki kelas" }

        // Load foto profil guru via Glide
        if (guru.photoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(guru.photoUrl)
                .placeholder(R.drawable.image_3)
                .error(R.drawable.image_3)
                .into(holder.ivFoto)
        } else {
            holder.ivFoto.setImageResource(R.drawable.image_3)
        }
    }

    override fun getItemCount() = list.size
}
