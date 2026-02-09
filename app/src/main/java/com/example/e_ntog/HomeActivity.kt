package com.example.e_ntog

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import android.widget.TextView
import androidx.core.view.GravityCompat

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Tombol Menu
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val menuButton = findViewById<ImageView>(R.id.iv_header_icon)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }


        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {

                }
                R.id.nav_logout -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }

            }
            drawerLayout.closeDrawers()
            true
        }

        // Terlambat
        val cardTerlambat = findViewById<MaterialCardView>(R.id.card_terlambat)
        cardTerlambat.setOnClickListener {
            val intent = Intent(this@HomeActivity, TerlambatActivity::class.java)
            startActivity(intent)
        }

        // Tidak Hadir
        val cardIzinHadir = findViewById<MaterialCardView>(R.id.card_izin_hadir)
        cardIzinHadir.setOnClickListener {
            val intent = Intent(this@HomeActivity, TidakHadirActivity::class.java)
            startActivity(intent)
        }

        // Dispen
        val cardDispen = findViewById<MaterialCardView>(R.id.card_dispen)
        cardDispen.setOnClickListener {
            val intent = Intent(this@HomeActivity, DispensasiActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences("DATA_IZIN", MODE_PRIVATE)

        val jumlahDispen = prefs.getInt("JUMLAH_IZIN_DISPENSASI", 0)
        val txtTotalDispen = findViewById<TextView>(R.id.txtTotalDispen)
        txtTotalDispen.text = jumlahDispen.toString()

        val jumlahTerlambat = prefs.getInt("JUMLAH_IZIN_TERLAMBAT", 0)
        val txtTotalIzinTerlambat = findViewById<TextView>(R.id.txtTotalIzinTerlambat)
        txtTotalIzinTerlambat.text = jumlahTerlambat.toString()

        val jumlahIzin = prefs.getInt("JUMLAH_IZIN_TIDAK_MASUK", 0)
        val txtTotalTidakHadir = findViewById<TextView>(R.id.txtTotalIzinTidakHadir)
        txtTotalTidakHadir.text = jumlahIzin.toString()
    }
}
