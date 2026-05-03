package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : BaseActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        if (auth.currentUser == null) {
            goTo(LoginActivity::class.java)
            return
        }

        checkUserRole()
    }

    private fun checkUserRole() {
        when (session.getRole()) {
            SessionManager.ROLE_GURU -> {
                goTo(HomeGuruActivity::class.java)
                return
            }
            SessionManager.ROLE_MURID -> {
                val uid = session.getUid()
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { snap ->
                        if ((snap.getString("kelasId") ?: "").isEmpty()) {
                            goTo(JoinKelasActivity::class.java)
                        } else {
                            showMuridDashboard()
                        }
                    }
                    .addOnFailureListener { showMuridDashboard() }
            }
            else -> showMuridDashboard()
        }
    }

    private fun showMuridDashboard() {
        setContentView(R.layout.activity_home)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        // Inisialisasi Header Menu (Nama & Kelas)
        val headerView = navigationView.getHeaderView(0)
        val tvNavName = headerView.findViewById<TextView>(R.id.tv_nav_name_siswa) // Tambahkan _siswa
        val tvNavClass = headerView.findViewById<TextView>(R.id.tv_nav_class_siswa)

        // Set Data Awal - Gunakan safe call (?) agar tidak crash jika ID salah
        findViewById<TextView>(R.id.tv_greeting)?.text = "Hi ${session.getNama()}"
        tvNavName?.text = session.getNama()

        // Buka Drawer dari Kanan
        findViewById<ImageView>(R.id.iv_header_icon)?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // Ambil Data Realtime dari Firestore
        db.collection("users").document(session.getUid())
            .addSnapshotListener { snap, _ ->
                snap?.let {
                    // Update Angka Rekapitulasi - TAMBAHKAN '?' SEBELUM '.text'
                    findViewById<TextView>(R.id.txtTotalIzinTerlambat)?.text = (it.getLong("totalTerlambat") ?: 0L).toString()
                    findViewById<TextView>(R.id.txtTotalIzinTidakHadir)?.text = (it.getLong("totalTidakHadir") ?: 0L).toString()
                    findViewById<TextView>(R.id.txtTotalDispen)?.text = (it.getLong("totalDispen") ?: 0L).toString()

                    // Update Nama & Kelas di Drawer
                    tvNavName?.text = it.getString("nama") ?: session.getNama()
                    tvNavClass?.text = it.getString("kelas") ?: "-"
                }
            }

        // Navigasi Menu Drawer (Hanya ditulis satu kali)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_chat_forum -> startActivity(Intent(this, ForumKelasActivity::class.java))
                R.id.nav_announcement -> startActivity(Intent(this, AnnouncementActivity::class.java))
                R.id.nav_logout -> {
                    auth.signOut()
                    session.clearSession()
                    goTo(LoginActivity::class.java)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        // Klik Card Menu (Hanya ditulis satu kali)
        findViewById<View>(R.id.card_terlambat).setOnClickListener {
            startActivity(Intent(this, TerlambatActivity::class.java))
        }
        findViewById<View>(R.id.card_izin_hadir).setOnClickListener {
            startActivity(Intent(this, TidakHadirActivity::class.java))
        }
        findViewById<View>(R.id.card_dispen).setOnClickListener {
            startActivity(Intent(this, DispensasiActivity::class.java))
        }
    }

    private fun goTo(cls: Class<*>) {
        startActivity(Intent(this, cls).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}