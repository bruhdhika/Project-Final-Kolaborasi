package com.example.e_ntog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 3 // <--- NAIK KE VERSI 3

        // --- TABEL 1: USERS (LOGIN) ---
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"

        // --- TABEL 2: TERLAMBAT (FORMULIR) ---
        private const val TABLE_TERLAMBAT = "terlambat"
        private const val COL_LATE_ID = "id"
        private const val COL_LATE_NAMA = "nama"
        private const val COL_LATE_KELAS = "kelas"
        private const val COL_LATE_ALASAN = "alasan"
        private const val COL_LATE_WALI = "wali_kelas"
        private const val COL_LATE_WAKTU = "waktu_input" // Tambahan: mencatat waktu otomatis
    }

    // 1. Membuat Tabel-Tabel
    override fun onCreate(db: SQLiteDatabase?) {
        // Query Buat Tabel Users
        val createTableUsers = ("CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_EMAIL TEXT)")
        db?.execSQL(createTableUsers)

        // Query Buat Tabel Terlambat
        val createTableTerlambat = ("CREATE TABLE $TABLE_TERLAMBAT (" +
                "$COL_LATE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_LATE_NAMA TEXT, " +
                "$COL_LATE_KELAS TEXT, " +
                "$COL_LATE_ALASAN TEXT, " +
                "$COL_LATE_WALI TEXT, " +
                "$COL_LATE_WAKTU TEXT)")
        db?.execSQL(createTableTerlambat)
    }

    // 2. Reset Database jika versi berubah
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Hapus tabel lama jika ada pembaruan versi
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TERLAMBAT")
        onCreate(db)
    }

    // --- FUNGSI USERS ---
    fun simpanAtauCekUser(email: String): Boolean {
        val dbRead = this.readableDatabase
        val cursor = dbRead.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?", arrayOf(email))
        val sudahAda = cursor.count > 0
        cursor.close()

        if (!sudahAda) {
            val dbWrite = this.writableDatabase
            val values = ContentValues()
            values.put(COLUMN_EMAIL, email)
            val result = dbWrite.insert(TABLE_USERS, null, values)
            dbWrite.close()
            return result != -1L
        }
        return true
    }

    // --- FUNGSI BARU: INSERT DATA TERLAMBAT ---
    fun insertTerlambat(nama: String, kelas: String, alasan: String, wali: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        // Ambil waktu sekarang otomatis
        val timeStamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        values.put(COL_LATE_NAMA, nama)
        values.put(COL_LATE_KELAS, kelas)
        values.put(COL_LATE_ALASAN, alasan)
        values.put(COL_LATE_WALI, wali)
        values.put(COL_LATE_WAKTU, timeStamp)

        val result = db.insert(TABLE_TERLAMBAT, null, values)
        db.close()

        // Jika result -1 artinya gagal, jika tidak berarti berhasil (mengembalikan ID baris)
        return result != -1L
    }
}