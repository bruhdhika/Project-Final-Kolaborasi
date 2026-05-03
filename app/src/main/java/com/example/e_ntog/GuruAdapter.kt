package com.example.e_ntog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GuruAdapter(
    private val listGuru: List<GuruModel>,
    private val onItemClick: (GuruModel) -> Unit
) : RecyclerView.Adapter<GuruAdapter.GuruViewHolder>() {

    inner class GuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // PERBAIKAN BARIS 16 & 17: Ganti 'view' jadi 'itemView'
        val namaGuru: TextView = itemView.findViewById(R.id.tvNamaGuru)
        val fotoGuru: ImageView = itemView.findViewById(R.id.ivFotoGuru)

        fun bind(guru: GuruModel) {
            // PERBAIKAN BARIS 20: Panggil 'namaGuru', bukan 'tvNamaGuru'
            namaGuru.text = guru.nama

            // Jika ada sistem foto, bisa menggunakan Glide di sini.
            // Untuk sementara kita gunakan icon default.
            // PERBAIKAN BARIS 24: Panggil 'fotoGuru', bukan 'ivFotoGuru'
            fotoGuru.setImageResource(R.drawable.profile)

            itemView.setOnClickListener {
                onItemClick(guru)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guru, parent, false)
        return GuruViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
        holder.bind(listGuru[position])
    }

    override fun getItemCount(): Int = listGuru.size
}