package com.example.e_ntog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class ProfileActivity : BaseActivity() {

    private val auth    = FirebaseAuth.getInstance()
    private val db      = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private lateinit var tvNama: TextView

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadProfile()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
setupBackButton()
        session = SessionManager(this)

        tvNama             = findViewById(R.id.namapengguna)
        val tvKelas        = findViewById<TextView>(R.id.kelasdefault)
        val imgAvatar      = findViewById<ImageView>(R.id.img_avatar)
        val btnBack        = findViewById<ImageButton>(R.id.btn_back)
        val btnEdit        = findViewById<ImageButton>(R.id.btn_edit)
        val btnLogout      = findViewById<android.view.View>(R.id.btn_logout)
        val btnDelete      = findViewById<android.view.View>(R.id.btn_delete)
        val btnKeluarKelas = findViewById<android.view.View>(R.id.btn_keluar_kelas)
        val txtTidakMasuk  = findStatTextView(0)
        val txtDispen      = findStatTextView(1)
        val txtTerlambat   = findStatTextView(2)

        btnBack.setOnClickListener { finish() }

        btnEdit.setOnClickListener {
            editLauncher.launch(Intent(this, EditDataActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        btnDelete.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Hapus Akun")
                .setMessage("Yakin ingin menghapus akun? Data tidak bisa dikembalikan.")
                .setPositiveButton("Hapus") { _, _ -> deleteAccount() }
                .setNegativeButton("Batal", null)
                .show()
        }

        // ===== TAMBAHAN CEK KELAS =====
        if (session.getRole() == SessionManager.ROLE_MURID) {
            db.collection("users").document(session.getUid()).get()
                .addOnSuccessListener { snap ->
                    val kelasId = snap.getString("kelasId") ?: ""
                    if (kelasId.isNotEmpty()) {
                        btnKeluarKelas.visibility = android.view.View.VISIBLE
                    }
                }
        }

        btnKeluarKelas.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Keluar Kelas")
                .setMessage("Yakin ingin keluar dari kelas ini?\nKamu bisa join kelas lain dengan kode baru.")
                .setPositiveButton("Keluar") { _, _ -> keluarDariKelas(btnKeluarKelas) }
                .setNegativeButton("Batal", null)
                .show()
        }
        // ===== END TAMBAHAN =====

        loadProfile()
    }

    private fun loadProfile() {
        val uid = session.getUid()
        if (uid.isEmpty()) return

        db.collection("users").document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                tvNama.text = snap.getString("nama") ?: "-"
                snap.getString("nama")?.let { session.updateNama(it) }

                val kelas = snap.getString("kelas")
                val tvKelas = findViewById<TextView>(R.id.kelasdefault)
                tvKelas.text = if (!kelas.isNullOrEmpty()) kelas else "Belum diisi"

                val photoUrl = snap.getString("photoUrl") ?: ""
                val imgAvatar = findViewById<ImageView>(R.id.img_avatar)
                if (photoUrl.isNotEmpty()) {
                    Glide.with(this).load(photoUrl).circleCrop()
                        .placeholder(R.drawable.image_3)
                        .into(imgAvatar)
                }

                updateStatCard(R.id.stat_tidak_masuk, snap.getLong("totalTidakHadir") ?: 0L)
                updateStatCard(R.id.stat_dispen,      snap.getLong("totalDispen")     ?: 0L)
                updateStatCard(R.id.stat_terlambat,   snap.getLong("totalTerlambat")  ?: 0L)
            }
    }

    private fun updateStatCard(tvId: Int, value: Long) {
        try { findViewById<TextView>(tvId)?.text = value.toString() }
        catch (_: Exception) { }
    }

    private fun findStatTextView(index: Int): TextView? = null

    // mau keluar kelas??????
    private fun keluarDariKelas(btnKeluarKelas: android.view.View) {
        val uid = session.getUid()

        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                val kelasId = snap.getString("kelasId") ?: ""

                if (kelasId.isNotEmpty()) {
                    db.collection("kelas").document(kelasId)
                        .update("jumlahMurid", FieldValue.increment(-1))
                }

                db.collection("users").document(uid)
                    .update(
                        "kelasId", "",
                        "kelasNama", ""
                    )
                    .addOnSuccessListener {
                        btnKeluarKelas.visibility = android.view.View.GONE
                        Toast.makeText(this, "Berhasil keluar dari kelas.", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this, JoinKelasActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal keluar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
    }
    // ===== END =====

    private fun deleteAccount() {
        val uid = session.getUid()
        db.collection("users").document(uid).delete()
            .addOnSuccessListener {
                auth.currentUser?.delete()
                session.clearSession()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
    }
}