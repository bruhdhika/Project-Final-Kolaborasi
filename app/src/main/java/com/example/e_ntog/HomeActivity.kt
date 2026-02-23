package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private val auth  = FirebaseAuth.getInstance()
    private val db    = FirebaseFirestore.getInstance()
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = SessionManager(this)

        // Guard: jika tidak login, ke LoginActivity
        if (auth.currentUser == null) {
            goTo(LoginActivity::class.java); return
        }



        // ROUTING BERDASARKAN ROLE
        when (session.getRole()) {
            SessionManager.ROLE_GURU -> {
                // Guru langsung ke dashboard guru
                goTo(HomeGuruActivity::class.java); return
            }
            SessionManager.ROLE_MURID -> {
                // Murid: cek apakah sudah join kelas
                val uid = session.getUid()
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { snap ->
                        val kelasId = snap.getString("kelasId") ?: ""
                        if (kelasId.isEmpty()) {
                            // Belum punya kelas → wajib join dulu
                            goTo(JoinKelasActivity::class.java); return@addOnSuccessListener
                        }
                        // Sudah punya kelas → tampilkan dashboard murid
                        showMuridDashboard()
                    }
                    .addOnFailureListener {
                        // Jika gagal load, tetap tampilkan dashboard
                        showMuridDashboard()
                    }
            }
            else -> showMuridDashboard() // fallback
        }
    }

    private fun showMuridDashboard() {
        setContentView(R.layout.activity_home)

        val drawerLayout   = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val ivMenuIcon     = findViewById<ImageView>(R.id.iv_header_icon)
        val tvGreeting     = findViewById<TextView>(R.id.tv_greeting)
        val txtTerlambat   = findViewById<TextView>(R.id.txtTotalIzinTerlambat)
        val txtTidakHadir  = findViewById<TextView>(R.id.txtTotalIzinTidakHadir)
        val txtDispen      = findViewById<TextView>(R.id.txtTotalDispen)
        val cardTerlambat  = findViewById<MaterialCardView>(R.id.card_terlambat)
        val cardIzinHadir  = findViewById<MaterialCardView>(R.id.card_izin_hadir)
        val cardDispen     = findViewById<MaterialCardView>(R.id.card_dispen)

        tvGreeting.text = "Hi ${session.getNama()}"

        // Load counter realtime dari Firestore
        db.collection("users").document(session.getUid())
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                txtTerlambat.text  = (snap.getLong("totalTerlambat")  ?: 0L).toString()
                txtTidakHadir.text = (snap.getLong("totalTidakHadir") ?: 0L).toString()
                txtDispen.text     = (snap.getLong("totalDispen")     ?: 0L).toString()
            }

        ivMenuIcon.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_logout  -> { auth.signOut(); session.clearSession(); goTo(LoginActivity::class.java) }
            }
            drawerLayout.closeDrawers(); true
        }

        cardTerlambat.setOnClickListener  { startActivity(Intent(this, TerlambatActivity::class.java)) }
        cardIzinHadir.setOnClickListener  { startActivity(Intent(this, TidakHadirActivity::class.java)) }
        cardDispen.setOnClickListener     { startActivity(Intent(this, DispensasiActivity::class.java)) }
    }

    private fun goTo(cls: Class<*>) {
        startActivity(Intent(this, cls).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}

