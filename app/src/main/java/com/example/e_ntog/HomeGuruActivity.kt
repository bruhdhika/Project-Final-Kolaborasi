package com.example.e_ntog

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class KelasModel(
    val kelasId: String = "",
    val namaKelas: String = "",
    val kodeKelas: String = "",
    val jumlahMurid: Long = 0L
)

class HomeGuruActivity : BaseActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private val kelasList = mutableListOf<KelasModel>()
    private lateinit var adapter: KelasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_guru)
        setupBackButton()

        session = SessionManager(this)

        val tvGreeting     = findViewById<TextView>(R.id.tv_guru_greeting)
        val rvKelas        = findViewById<RecyclerView>(R.id.rv_kelas)
        val fabTambah      = findViewById<FloatingActionButton>(R.id.fab_tambah_kelas)
        val tvEmpty        = findViewById<TextView>(R.id.tv_empty_kelas)
        val ivMenuIcon     = findViewById<ImageView>(R.id.iv_header_icon)
        val drawerLayout   = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        tvGreeting.text = "Halo, ${session.getNama()}"

        // ── Setup RecyclerView ──────────────────────────────────────────────
        adapter = KelasAdapter(
            kelasList,
            onKelasClick = { kelas ->
                startActivity(Intent(this, KelasMuridActivity::class.java).apply {
                    putExtra("KELAS_ID",   kelas.kelasId)
                    putExtra("KELAS_NAMA", kelas.namaKelas)
                    putExtra("KELAS_KODE", kelas.kodeKelas)
                })
            },
            onDeleteClick = { kelas -> showDialogHapusKelas(kelas) }
        )
        rvKelas.layoutManager = LinearLayoutManager(this)
        rvKelas.adapter = adapter

        // ── Drawer ─────────────────────────────────────────────────────────
        ivMenuIcon.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile      -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_chat_forum   -> startActivity(Intent(this, ForumKelasActivity::class.java))
                R.id.nav_announcement -> startActivity(Intent(this, AnnouncementActivity::class.java))
                R.id.nav_logout       -> {
                    auth.signOut(); session.clearSession()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            }
            drawerLayout.closeDrawers(); true
        }

        fabTambah.setOnClickListener { showDialogBuatKelas() }

        // ── Load kelas realtime ─────────────────────────────────────────────
        loadKelasList(tvEmpty, rvKelas)
    }

    private fun loadKelasList(tvEmpty: TextView, rvKelas: RecyclerView) {
        db.collection("kelas")
            .whereEqualTo("guruUid", session.getUid())
            .addSnapshotListener { snaps, error ->
                if (error != null) {
                    Toast.makeText(this, "Error load kelas: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val docs = snaps?.documents ?: emptyList()
                kelasList.clear()

                if (docs.isEmpty()) {
                    adapter.notifyDataSetChanged()
                    tvEmpty.visibility  = View.VISIBLE
                    rvKelas.visibility  = View.GONE
                    return@addSnapshotListener
                }

                tvEmpty.visibility = View.GONE
                rvKelas.visibility = View.VISIBLE

                // Untuk setiap kelas, hitung jumlah murid realtime
                var loaded = 0
                docs.forEach { doc ->
                    val kelasId = doc.id
                    db.collection("users")
                        .whereEqualTo("kelasId", kelasId)
                        .get()
                        .addOnSuccessListener { muridSnap ->
                            val jumlah = muridSnap.documents
                                .count { it.getString("role") == "murid" }
                            kelasList.add(KelasModel(
                                kelasId     = kelasId,
                                namaKelas   = doc.getString("namaKelas") ?: "",
                                kodeKelas   = doc.getString("kodeKelas") ?: "",
                                jumlahMurid = jumlah.toLong()
                            ))
                            loaded++
                            if (loaded == docs.size) {
                                // Sort berdasarkan namaKelas agar urutan konsisten
                                kelasList.sortBy { it.namaKelas }
                                adapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener {
                            kelasList.add(KelasModel(
                                kelasId   = kelasId,
                                namaKelas = doc.getString("namaKelas") ?: "",
                                kodeKelas = doc.getString("kodeKelas") ?: "",
                                jumlahMurid = 0L
                            ))
                            loaded++
                            if (loaded == docs.size) adapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun showDialogBuatKelas() {
        val etNamaKelas = EditText(this).apply {
            hint = "Nama kelas, contoh: XI RPL 1"
            setPadding(40, 20, 40, 20)
        }
        AlertDialog.Builder(this)
            .setTitle("Buat Kelas Baru")
            .setView(etNamaKelas)
            .setPositiveButton("Buat") { _, _ ->
                val namaKelas = etNamaKelas.text.toString().trim()
                if (namaKelas.isNotEmpty()) buatKelas(namaKelas)
                else Toast.makeText(this, "Nama kelas tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun buatKelas(namaKelas: String) {
        val kodeKelas = generateKodeKelas()
        db.collection("kelas").add(hashMapOf(
            "namaKelas"   to namaKelas,
            "kodeKelas"   to kodeKelas,
            "guruUid"     to session.getUid(),
            "guruNama"    to session.getNama(),
            "jumlahMurid" to 0L,
            "createdAt"   to FieldValue.serverTimestamp()
        )).addOnSuccessListener {
            Toast.makeText(this, "Kelas '$namaKelas' dibuat!\nKode: $kodeKelas", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal buat kelas: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /** Kode 6 karakter: huruf kapital + angka, hindari 0/O/1/I agar tidak membingungkan */
    private fun generateKodeKelas(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun showDialogHapusKelas(kelas: KelasModel) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kelas")
            .setMessage("Yakin hapus kelas '${kelas.namaKelas}'?\nSemua murid akan otomatis keluar.")
            .setPositiveButton("Hapus") { _, _ ->
                db.collection("kelas").document(kelas.kelasId).delete()
                    .addOnSuccessListener {
                        // Reset kelasId semua murid di kelas ini
                        db.collection("users")
                            .whereEqualTo("kelasId", kelas.kelasId)
                            .get()
                            .addOnSuccessListener { snaps ->
                                snaps.forEach { doc ->
                                    doc.reference.update("kelasId", "", "kelasNama", "")
                                }
                            }
                        Toast.makeText(this, "Kelas '${kelas.namaKelas}' dihapus.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal hapus: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
