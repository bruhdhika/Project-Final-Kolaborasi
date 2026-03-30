package com.example.e_ntog

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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

    private val auth    = FirebaseAuth.getInstance()
    private val db      = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private val kelasList = mutableListOf<KelasModel>()
    private lateinit var adapter: KelasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_guru)

        session = SessionManager(this)
setupBackButton()
        val tvGreeting     = findViewById<TextView>(R.id.tv_guru_greeting)
        val rvKelas        = findViewById<RecyclerView>(R.id.rv_kelas)
        val fabTambah      = findViewById<FloatingActionButton>(R.id.fab_tambah_kelas)
        val tvEmpty        = findViewById<TextView>(R.id.tv_empty_kelas)
        val ivMenuIcon     = findViewById<ImageView>(R.id.iv_header_icon)
        val drawerLayout   = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        tvGreeting.text = "Halo, ${session.getNama()}"

        // Setup RecyclerView
        adapter = KelasAdapter(
            kelasList,
            onKelasClick = { kelas ->
                val intent = Intent(this, KelasMuridActivity::class.java)
                intent.putExtra("KELAS_ID",   kelas.kelasId)
                intent.putExtra("KELAS_NAMA", kelas.namaKelas)
                intent.putExtra("KELAS_KODE", kelas.kodeKelas)
                startActivity(intent)
            },
            onDeleteClick = { kelas ->
                showDialogHapusKelas(kelas)
            }
        )
        rvKelas.layoutManager = LinearLayoutManager(this)
        rvKelas.adapter = adapter

        // Drawer menu
        ivMenuIcon.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_chat_forum -> startActivity(Intent(this, ForumKelasActivity::class.java))
                R.id.nav_announcement -> startActivity(Intent(this, AnnouncementActivity::class.java))
                R.id.nav_logout  -> {
                    auth.signOut(); session.clearSession()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            }
            drawerLayout.closeDrawers(); true
        }

        // FAB: buat kelas baru
        fabTambah.setOnClickListener { showDialogBuatKelas() }

        // Load semua kelas milik guru ini dari Firestore
        loadKelasList(tvEmpty)
    }

    private fun loadKelasList(tvEmpty: TextView) {
        db.collection("kelas")
            .whereEqualTo("guruUid", session.getUid())
            .addSnapshotListener { snaps, _ ->
                kelasList.clear()
                val docs = snaps?.documents ?: emptyList()
                if (docs.isEmpty()) {
                    adapter.notifyDataSetChanged()
                    tvEmpty.visibility = View.VISIBLE
                    return@addSnapshotListener
                }
                tvEmpty.visibility = View.GONE
                var loaded = 0
                docs.forEach { doc ->
                    val kelasId = doc.id
                    // Hitung murid secara realtime dari collection users
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
                                adapter.notifyDataSetChanged()
                            }
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
        // Kode Kelas 6 digit
        val kodeKelas = generateKodeKelas()

        val kelasData = hashMapOf(
            "namaKelas"   to namaKelas,
            "kodeKelas"   to kodeKelas,
            "guruUid"     to session.getUid(),
            "guruNama"    to session.getNama(),
            "jumlahMurid" to 0L,
            "createdAt"   to FieldValue.serverTimestamp()
        )

        db.collection("kelas").add(kelasData)
            .addOnSuccessListener {
                Toast.makeText(this,
                    "Kelas '$namaKelas' berhasil dibuat! Kode: $kodeKelas",
                    Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal buat kelas: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Generate kode 6 karakter unik dari huruf kapital + angka.
     * Contoh: A3F7K2, BX92LM
     * Kecil kemungkinan bentrok karena ada 36^6 = 2 miliar kombinasi.
     */
    private fun generateKodeKelas(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // hindari 0,O,1,I agar tidak membingungkan
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun showDialogHapusKelas(kelas: KelasModel) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kelas")
            .setMessage("Yakin ingin menghapus kelas '${kelas.namaKelas}'?\nSemua murid di kelas ini akan otomatis keluar.")
            .setPositiveButton("Hapus") { _, _ ->
                db.collection("kelas").document(kelas.kelasId)
                    .delete()
                    .addOnSuccessListener {
                        // Reset kelasId semua murid yang ada di kelas ini
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

    private fun hapusKelas(idKelas: String) {
        db.collection("kelas").document(idKelas)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Kelas berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
