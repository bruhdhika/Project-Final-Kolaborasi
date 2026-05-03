package com.example.e_ntog

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : BaseActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager
    private lateinit var tvNama: TextView
    private lateinit var tvKelas: TextView

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

        tvNama = findViewById(R.id.namapengguna)
        tvKelas = findViewById(R.id.kelasdefault)
        val btnEdit = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_edit)
        val btnLogout = findViewById<View>(R.id.btn_logout)
        val btnDelete = findViewById<View>(R.id.btn_delete)
        val btnKeluarKelas = findViewById<View>(R.id.btn_keluar_kelas)

        loadProfile()

        btnEdit.setOnClickListener {
            val intent = Intent(this, EditDataActivity::class.java)
            editLauncher.launch(intent)
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar dari akun?")
                .setPositiveButton("Logout") { _, _ ->
                    auth.signOut()
                    session.clearSession()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }.setNegativeButton("Batal", null).show()
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Akun")
                .setMessage("Data kamu tidak bisa dikembalikan. Yakin?")
                .setPositiveButton("Hapus") { _, _ -> deleteAccount() }
                .setNegativeButton("Batal", null).show()
        }

        btnKeluarKelas.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Keluar Kelas")
                .setMessage("Kamu tidak akan lagi terdaftar di kelas ini.")
                .setPositiveButton("Keluar") { _, _ -> keluarDariKelas(btnKeluarKelas) }
                .setNegativeButton("Batal", null).show()
        }
    }

    private fun loadProfile() {
        val uid = session.getUid()
        db.collection("users").document(uid).addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener

            // Set Nama & Kelas
            tvNama.text = snap.getString("nama") ?: "User"
            tvKelas.text = snap.getString("kelasNama") ?: "Belum Join Kelas"

            // Ambil ID Stat dari XML yang baru (Gunakan ID yang sudah disesuaikan)
            findViewById<TextView>(R.id.tv_count_tidak_hadir)?.text = (snap.getLong("totalTidakHadir") ?: 0L).toString()
            findViewById<TextView>(R.id.tv_count_dispen)?.text     = (snap.getLong("totalDispen")     ?: 0L).toString()
            findViewById<TextView>(R.id.tv_count_terlambat)?.text  = (snap.getLong("totalTerlambat")  ?: 0L).toString()

            // Tombol Keluar Kelas khusus Murid
            val kelasId = snap.getString("kelasId") ?: ""
            if (session.getRole() == SessionManager.ROLE_MURID && kelasId.isNotEmpty()) {
                findViewById<View>(R.id.btn_keluar_kelas).visibility = View.VISIBLE
            } else {
                findViewById<View>(R.id.btn_keluar_kelas).visibility = View.GONE
            }

            // Update Foto Profil pakai Glide
            val photoUrl = snap.getString("photoUrl") ?: ""
            val imgAvatar = findViewById<ImageView>(R.id.img_avatar)
            if (photoUrl.isNotEmpty()) {
                Glide.with(this).load(photoUrl).circleCrop()
                    .placeholder(R.drawable.image_3).into(imgAvatar)
            }
        }
    }

    private fun keluarDariKelas(btn: View) {
        val uid = session.getUid()
        db.collection("users").document(uid).get().addOnSuccessListener { snap ->
            val kelasId = snap.getString("kelasId") ?: ""
            if (kelasId.isNotEmpty()) {
                db.collection("kelas").document(kelasId).update("jumlahMurid", FieldValue.increment(-1))
                db.collection("users").document(uid).update("kelasId", "", "kelasNama", "").addOnSuccessListener {
                    btn.visibility = View.GONE
                    Toast.makeText(this, "Berhasil keluar kelas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteAccount() {
        db.collection("users").document(session.getUid()).delete().addOnSuccessListener {
            auth.currentUser?.delete()
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}