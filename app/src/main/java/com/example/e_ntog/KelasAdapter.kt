package com.example.e_ntog

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class KelasAdapter(
    private val list: List<KelasModel>,
    private val onKelasClick: (KelasModel) -> Unit,
    private val onDeleteClick: (KelasModel) -> Unit
) : RecyclerView.Adapter<KelasAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardKelas:   MaterialCardView = view.findViewById(R.id.card_kelas)
        val tvNamaKelas: TextView         = view.findViewById(R.id.tv_nama_kelas)
        val tvKodeKelas: TextView         = view.findViewById(R.id.tv_kode_kelas)
        val tvJumlahMurid: TextView       = view.findViewById(R.id.tv_jumlah_murid)
        val btnDelete: View = view.findViewById(R.id.btn_delete_kelas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kelas_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kelas = list[position]
        holder.tvNamaKelas.text   = kelas.namaKelas
        holder.tvKodeKelas.text   = "Kode: ${kelas.kodeKelas}"
        holder.tvJumlahMurid.text = "${kelas.jumlahMurid} murid terdaftar"
        holder.cardKelas.setOnClickListener { onKelasClick(kelas) }

        holder.btnDelete.setOnClickListener { onDeleteClick(kelas) }
    }

    override fun getItemCount() = list.size
}
