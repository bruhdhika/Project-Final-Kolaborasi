package com.example.e_ntog

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class MuridKelasModel(
    val uid: String = "",
    val nama: String = "",
    val kelas: String = "",
    val totalTerlambat: Long = 0L,
    val totalTidakHadir: Long = 0L,
    val totalDispen: Long = 0L
)

data class IzinModel(
    val docId: String = "",
    val tanggal: String = "",
    val alasan: String = "",
    val status: String = "pending",
    val tipe: String = ""
)

class KelasMuridActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val muridList = mutableListOf<MuridKelasModel>()
    private lateinit var adapter: MuridKelasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kelas_murid)

        val kelasId   = intent.getStringExtra("KELAS_ID")   ?: ""
        val kelasNama = intent.getStringExtra("KELAS_NAMA") ?: ""
        val kelasKode = intent.getStringExtra("KELAS_KODE") ?: ""

        val tvJudul = findViewById<TextView>(R.id.tv_judul_kelas)
        val tvKode  = findViewById<TextView>(R.id.tv_kode_kelas_detail)
        val rvMurid = findViewById<RecyclerView>(R.id.rv_murid_kelas)
        val tvEmpty = findViewById<TextView>(R.id.tv_empty_murid)
        val btnBack = findViewById<ImageView>(R.id.iv_back_arrow)

        tvJudul.text = kelasNama
        tvKode.text  = "Kode Kelas: $kelasKode"
        btnBack.setOnClickListener { finish() }

        adapter = MuridKelasAdapter(muridList) { murid -> showPilihTipeIzinDialog(murid) }
        rvMurid.layoutManager = LinearLayoutManager(this)
        rvMurid.adapter = adapter

        // ── Query tunggal, filter role di sisi klien (tidak butuh composite index) ──
        db.collection("users")
            .whereEqualTo("kelasId", kelasId)
            .addSnapshotListener { snaps, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                muridList.clear()
                snaps?.forEach { doc ->
                    if (doc.getString("role") == "murid") {
                        muridList.add(MuridKelasModel(
                            uid             = doc.id,
                            nama            = doc.getString("nama")           ?: "-",
                            kelas           = doc.getString("kelasNama")      ?: "-",
                            totalTerlambat  = doc.getLong("totalTerlambat")   ?: 0L,
                            totalTidakHadir = doc.getLong("totalTidakHadir")  ?: 0L,
                            totalDispen     = doc.getLong("totalDispen")      ?: 0L
                        ))
                    }
                }
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (muridList.isEmpty()) View.VISIBLE else View.GONE
                rvMurid.visibility = if (muridList.isEmpty()) View.GONE   else View.VISIBLE
            }
    }

    private fun showPilihTipeIzinDialog(murid: MuridKelasModel) {
        val tipes = arrayOf(
            "Terlambat (${murid.totalTerlambat}x)",
            "Tidak Hadir (${murid.totalTidakHadir}x)",
            "Dispensasi (${murid.totalDispen}x)"
        )
        val subcollections = arrayOf("history_terlambat", "history_tidak_hadir", "history_dispen")

        AlertDialog.Builder(this)
            .setTitle("${murid.nama} — Pilih Jenis Izin")
            .setItems(tipes) { _, which ->
                loadIzinMurid(murid, subcollections[which], tipes[which])
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    private fun loadIzinMurid(murid: MuridKelasModel, subcollection: String, label: String) {
        db.collection("users").document(murid.uid)
            .collection(subcollection)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    Toast.makeText(this, "Belum ada riwayat $label", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val izinList = docs.map { doc ->
                    IzinModel(
                        docId   = doc.id,
                        tanggal = doc.getString("tanggal") ?: "-",
                        alasan  = doc.getString("alasan")  ?: "-",
                        status  = doc.getString("status")  ?: "pending",
                        tipe    = subcollection
                    )
                }
                showIzinListDialog(murid, izinList, label)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal load riwayat: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showIzinListDialog(murid: MuridKelasModel, izinList: List<IzinModel>, label: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_izin_list, null)
        val rvIzin  = dialogView.findViewById<RecyclerView>(R.id.rv_izin_list)
        val tvLabel = dialogView.findViewById<TextView>(R.id.tv_dialog_label)
        tvLabel.text = "${murid.nama} — $label"

        // Buat list yang bisa dimutasi agar bisa refresh setelah approve/reject
        val mutableIzinList = izinList.toMutableList()

        val izinAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_izin_guru, parent, false)
                ) {}

            override fun getItemCount() = mutableIzinList.size

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
                val izin = mutableIzinList[pos]
                val view = holder.itemView
                view.findViewById<TextView>(R.id.tv_izin_tanggal).text = izin.tanggal
                view.findViewById<TextView>(R.id.tv_izin_alasan).text  = izin.alasan

                val tvStatus   = view.findViewById<TextView>(R.id.tv_izin_status)
                val btnSetujui = view.findViewById<Button>(R.id.btn_setujui)
                val btnTolak   = view.findViewById<Button>(R.id.btn_tolak)

                when (izin.status) {
                    "disetujui" -> {
                        tvStatus.text = "✅ Disetujui"
                        tvStatus.setTextColor(0xFF2E7D32.toInt())
                        btnSetujui.visibility = View.GONE
                        btnTolak.visibility   = View.GONE
                    }
                    "ditolak" -> {
                        tvStatus.text = "❌ Ditolak"
                        tvStatus.setTextColor(0xFFB71C1C.toInt())
                        btnSetujui.visibility = View.GONE
                        btnTolak.visibility   = View.GONE
                    }
                    else -> {
                        tvStatus.text = "⏳ Menunggu"
                        tvStatus.setTextColor(0xFFE65100.toInt())
                        btnSetujui.visibility = View.VISIBLE
                        btnTolak.visibility   = View.VISIBLE
                    }
                }

                btnSetujui.setOnClickListener {
                    updateStatusIzin(murid.uid, izin.tipe, izin.docId, "disetujui")
                    mutableIzinList[pos] = izin.copy(status = "disetujui")
                    notifyItemChanged(pos)
                }
                btnTolak.setOnClickListener {
                    updateStatusIzin(murid.uid, izin.tipe, izin.docId, "ditolak")
                    mutableIzinList[pos] = izin.copy(status = "ditolak")
                    notifyItemChanged(pos)
                }
            }
        }

        rvIzin.layoutManager = LinearLayoutManager(this)
        rvIzin.adapter = izinAdapter

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Tutup", null)
            .show()
    }

    private fun updateStatusIzin(
        muridUid: String, subcollection: String, docId: String, statusBaru: String
    ) {
        db.collection("users").document(muridUid)
            .collection(subcollection).document(docId)
            .update("status", statusBaru)
            .addOnSuccessListener {
                val label = if (statusBaru == "disetujui") "disetujui ✅" else "ditolak ❌"
                Toast.makeText(this, "Izin $label", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal update: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
