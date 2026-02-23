package com.example.e_ntog

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class MuridKelasAdapter(
    private val list: List<MuridKelasModel>,
    private val onMuridClick: (MuridKelasModel) -> Unit
) : RecyclerView.Adapter<MuridKelasAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardMurid:     MaterialCardView = view.findViewById(R.id.card_murid)
        val tvNama:        TextView         = view.findViewById(R.id.tv_murid_nama)
        val tvKelas:       TextView         = view.findViewById(R.id.tv_murid_kelas)
        val tvStatTerlambat: TextView       = view.findViewById(R.id.tv_stat_terlambat)
        val tvStatAbsen:   TextView         = view.findViewById(R.id.tv_stat_absen)
        val tvStatDispen:  TextView         = view.findViewById(R.id.tv_stat_dispen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_murid_kelas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val murid = list[position]
        holder.tvNama.text          = murid.nama
        holder.tvKelas.text         = murid.kelas.ifEmpty { "Kelas belum diisi" }
        holder.tvStatTerlambat.text = "Terlambat: ${murid.totalTerlambat}x"
        holder.tvStatAbsen.text     = "Tdk Hadir: ${murid.totalTidakHadir}x"
        holder.tvStatDispen.text    = "Dispen: ${murid.totalDispen}x"
        holder.cardMurid.setOnClickListener { onMuridClick(murid) }
    }

    override fun getItemCount() = list.size
}
