package com.example.e_ntog

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class AnnouncementModel(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val guruNama: String = "",
    val kelasNama: String = "",
    val timestamp: Timestamp? = null
)

class AnnouncementActivity : BaseActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private val annoList = mutableListOf<AnnouncementModel>()
    private lateinit var adapter: AnnouncementAdapter

    // Untuk guru: list semua kelas yang dia punya
    private val kelasMilikGuru = mutableListOf<KelasModel>()
    // kelasId aktif yang sedang dilihat (untuk murid: kelas mereka; untuk guru: kelas terpilih)
    private var activeKelasId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement)
        setupBackButton()

        session = SessionManager(this)

        val rvAnno   = findViewById<RecyclerView>(R.id.rv_announcement)
        val fabKirim = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fab_kirim_announcement)
        val ivBack   = findViewById<ImageView>(R.id.iv_back_anno)
        val tvEmpty  = findViewById<TextView>(R.id.tv_anno_empty)
        val tvTitle  = findViewById<TextView?>(R.id.tv_anno_title_header)

        ivBack.setOnClickListener { finish() }

        adapter = AnnouncementAdapter(annoList)
        rvAnno.layoutManager = LinearLayoutManager(this)
        rvAnno.adapter = adapter

        val isGuru = session.getRole() == SessionManager.ROLE_GURU

        if (isGuru) {
            // ── GURU FLOW ──────────────────────────────────────────────────
            // FAB selalu tampil untuk guru
            fabKirim.visibility = View.VISIBLE
            tvTitle?.text = "Pengumuman Saya"

            // Load semua kelas milik guru, lalu tampilkan selector
            loadKelasGuru(tvEmpty, rvAnno)

            fabKirim.setOnClickListener { showDialogPilihKelasDanKirim() }

        } else {
            // ── MURID FLOW ─────────────────────────────────────────────────
            fabKirim.visibility = View.GONE
            tvTitle?.text = "Pengumuman"

            // Murid baca pengumuman dari kelasnya saja
            db.collection("users").document(session.getUid()).get()
                .addOnSuccessListener { doc ->
                    activeKelasId = doc.getString("kelasId") ?: ""
                    if (activeKelasId.isEmpty()) {
                        tvEmpty.text       = "Kamu belum join kelas"
                        tvEmpty.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }
                    loadAnnouncements(activeKelasId, tvEmpty)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal load data: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // ── Guru: load semua kelas yang dia punya ─────────────────────────────
    private fun loadKelasGuru(tvEmpty: TextView, rvAnno: RecyclerView) {
        db.collection("kelas")
            .whereEqualTo("guruUid", session.getUid())
            .get()
            .addOnSuccessListener { snaps ->
                kelasMilikGuru.clear()
                snaps.forEach { doc ->
                    kelasMilikGuru.add(KelasModel(
                        kelasId   = doc.id,
                        namaKelas = doc.getString("namaKelas") ?: "",
                        kodeKelas = doc.getString("kodeKelas") ?: ""
                    ))
                }
                if (kelasMilikGuru.isEmpty()) {
                    tvEmpty.text       = "Kamu belum membuat kelas.\nBuat kelas dulu di halaman utama."
                    tvEmpty.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }
                // Default tampilkan pengumuman kelas pertama
                activeKelasId = kelasMilikGuru[0].kelasId
                loadAnnouncements(activeKelasId, tvEmpty)
                showKelasSelector()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal load kelas: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Tampilkan chip/dialog pilih kelas untuk guru
    private fun showKelasSelector() {
        val tvKelasSelector = findViewById<TextView?>(R.id.tv_kelas_selector) ?: return
        tvKelasSelector.visibility = View.VISIBLE
        updateKelasSelector(tvKelasSelector)
        tvKelasSelector.setOnClickListener {
            val namaKelas = kelasMilikGuru.map { it.namaKelas }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Pilih Kelas")
                .setItems(namaKelas) { _, which ->
                    activeKelasId = kelasMilikGuru[which].kelasId
                    updateKelasSelector(tvKelasSelector)
                    annoList.clear()
                    adapter.notifyDataSetChanged()
                    val tvEmpty = findViewById<TextView>(R.id.tv_anno_empty)
                    loadAnnouncements(activeKelasId, tvEmpty)
                }
                .show()
        }
    }

    private fun updateKelasSelector(tv: TextView) {
        val nama = kelasMilikGuru.find { it.kelasId == activeKelasId }?.namaKelas ?: ""
        tv.text = "Kelas: $nama  ▼"
    }

    // ── Load pengumuman dari kelas tertentu ───────────────────────────────
    private fun loadAnnouncements(kelasId: String, tvEmpty: TextView) {
        db.collection("kelas").document(kelasId)
            .collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snaps, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                annoList.clear()
                snaps?.forEach { doc ->
                    annoList.add(AnnouncementModel(
                        id        = doc.id,
                        title     = doc.getString("title")     ?: "",
                        content   = doc.getString("content")   ?: "",
                        guruNama  = doc.getString("guruNama")  ?: "",
                        kelasNama = doc.getString("kelasNama") ?: "",
                        timestamp = doc.getTimestamp("timestamp")
                    ))
                }
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (annoList.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    // ── Guru: dialog pilih kelas lalu kirim pengumuman ───────────────────
    private fun showDialogPilihKelasDanKirim() {
        if (kelasMilikGuru.isEmpty()) {
            Toast.makeText(this, "Kamu belum punya kelas", Toast.LENGTH_SHORT).show()
            return
        }

        val view      = layoutInflater.inflate(R.layout.dialog_kirim_announcement, null)
        val etTitle   = view.findViewById<EditText>(R.id.et_anno_title)
        val etContent = view.findViewById<EditText>(R.id.et_anno_content)
        val spinnerKelas = view.findViewById<Spinner?>(R.id.spinner_kelas_announcement)

        // Isi spinner kelas jika ada di layout
        spinnerKelas?.let { sp ->
            val namaList = kelasMilikGuru.map { it.namaKelas }
            sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaList)
                .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            // Default pilih kelas yang sedang ditampilkan
            val idx = kelasMilikGuru.indexOfFirst { it.kelasId == activeKelasId }
            if (idx >= 0) sp.setSelection(idx)
        }

        AlertDialog.Builder(this)
            .setTitle("Kirim Pengumuman")
            .setView(view)
            .setPositiveButton("Kirim") { _, _ ->
                val title   = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()
                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(this, "Judul dan isi wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Tentukan kelas tujuan: dari spinner jika ada, otherwise activeKelasId
                val targetIdx   = spinnerKelas?.selectedItemPosition ?: kelasMilikGuru.indexOfFirst { it.kelasId == activeKelasId }
                val targetKelas = if (targetIdx in kelasMilikGuru.indices) kelasMilikGuru[targetIdx] else return@setPositiveButton

                db.collection("kelas").document(targetKelas.kelasId)
                    .collection("announcements")
                    .add(hashMapOf(
                        "title"     to title,
                        "content"   to content,
                        "guruNama"  to session.getNama(),
                        "guruUid"   to session.getUid(),
                        "kelasNama" to targetKelas.namaKelas,
                        "timestamp" to FieldValue.serverTimestamp()
                    ))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pengumuman terkirim ke ${targetKelas.namaKelas}!", Toast.LENGTH_SHORT).show()
                        // Update tampilan ke kelas yang baru dikirim
                        if (activeKelasId != targetKelas.kelasId) {
                            activeKelasId = targetKelas.kelasId
                            val tvKelasSelector = findViewById<TextView?>(R.id.tv_kelas_selector)
                            tvKelasSelector?.let { updateKelasSelector(it) }
                            annoList.clear()
                            adapter.notifyDataSetChanged()
                            loadAnnouncements(activeKelasId, findViewById(R.id.tv_anno_empty))
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
