package com.example.e_ntog

import android.content.Context
import android.content.SharedPreferences

/**
 * SessionManager: Menyimpan state login user secara lokal.
 * Digunakan agar user tidak perlu login ulang setiap buka app.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME    = "EZinSession"
        const val KEY_UID       = "uid"
        const val KEY_EMAIL     = "email"
        const val KEY_NAMA      = "nama"
        const val KEY_ROLE      = "role"
        const val ROLE_MURID    = "murid"
        const val ROLE_GURU     = "guru"
    }

    fun saveSession(uid: String, email: String, nama: String, role: String) {
        prefs.edit()
            .putString(KEY_UID,   uid)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAMA,  nama)
            .putString(KEY_ROLE,  role)
            .apply()
    }

    fun getUid():   String = prefs.getString(KEY_UID,   "") ?: ""
    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    fun getNama():  String = prefs.getString(KEY_NAMA,  "") ?: ""
    fun getRole():  String = prefs.getString(KEY_ROLE,  "") ?: ""

    fun isLoggedIn(): Boolean = getUid().isNotEmpty()

    fun clearSession() = prefs.edit().clear().apply()

    fun updateNama(nama: String) = prefs.edit().putString(KEY_NAMA, nama).apply()
}
