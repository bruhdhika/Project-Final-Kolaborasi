package com.example.e_ntog

import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class EditDataActivity : BaseActivity() {

    private val auth    = FirebaseAuth.getInstance()
    private val db      = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var session: SessionManager
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this).load(it).circleCrop()
                .into(findViewById<CircleImageView>(R.id.edit_img_avatar))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data)
        setupBackButton()

        session = SessionManager(this)

        val etNama        = findViewById<EditText>(R.id.et_edit_nama)
        val etEmail       = findViewById<EditText>(R.id.et_edit_email)
        val etPassword    = findViewById<EditText>(R.id.et_edit_password)
        val etConfirm     = findViewById<EditText>(R.id.et_confirm_password)
        val layoutConfirm = findViewById<android.widget.LinearLayout>(R.id.layout_konfirmasi_pass)
        val btnSimpan     = findViewById<Button>(R.id.btn_simpan_perubahan)
        val btnChangePhoto= findViewById<ImageButton>(R.id.btn_change_photo)
        val imgAvatar     = findViewById<CircleImageView>(R.id.edit_img_avatar)

        val uid = session.getUid()

        // Load data profil saat ini
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                etNama.setText(snap.getString("nama")  ?: "")
                etEmail.setText(snap.getString("email") ?: "")
                val photoUrl = snap.getString("photoUrl") ?: ""
                if (photoUrl.isNotEmpty())
                    Glide.with(this).load(photoUrl).circleCrop().into(imgAvatar)
            }

        // Tampilkan field konfirmasi saat user mulai isi password baru
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                layoutConfirm.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        btnChangePhoto.setOnClickListener { pickImageLauncher.launch("image/*") }

        btnSimpan.setOnClickListener {
            val namaBaru    = etNama.text.toString().trim()
            val passBaru    = etPassword.text.toString().trim()
            val passConfirm = etConfirm.text.toString().trim()

            if (namaBaru.isEmpty()) {
                etNama.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }
            if (passBaru.isNotEmpty()) {
                if (passBaru.length < 6) {
                    etPassword.error = "Minimal 6 karakter"
                    return@setOnClickListener
                }
                if (passBaru != passConfirm) {
                    etConfirm.error = "Password tidak cocok"
                    return@setOnClickListener
                }
            }

            btnSimpan.isEnabled = false

            if (selectedImageUri != null) {
                uploadFotoThenSave(uid, namaBaru, passBaru, btnSimpan)
            } else {
                saveData(uid, namaBaru, passBaru, null, btnSimpan)
            }
        }
    }

    private fun uploadFotoThenSave(uid: String, nama: String, pass: String, btn: Button) {
        storage.reference.child("profile_photos/$uid.jpg")
            .putFile(selectedImageUri!!)
            .addOnSuccessListener { task ->
                task.storage.downloadUrl.addOnSuccessListener { uri ->
                    saveData(uid, nama, pass, uri.toString(), btn)
                }
            }
            .addOnFailureListener {
                btn.isEnabled = true
                Toast.makeText(this, "Gagal upload foto: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveData(uid: String, nama: String, pass: String, photoUrl: String?, btn: Button) {
        val updates = mutableMapOf<String, Any>("nama" to nama)
        if (!photoUrl.isNullOrEmpty()) updates["photoUrl"] = photoUrl

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                session.updateNama(nama)

                if (pass.isNotEmpty()) {
                    // Untuk update password, perlu re-autentikasi terlebih dahulu
                    updatePasswordWithReauth(pass, btn)
                } else {
                    finishWithSuccess(btn)
                }
            }
            .addOnFailureListener {
                btn.isEnabled = true
                Toast.makeText(this, "Gagal simpan: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Update password dengan re-autentikasi.
     * Firebase mengharuskan user re-login sebelum ubah password
     * jika sudah lama tidak login (token kadaluarsa).
     */
    private fun updatePasswordWithReauth(newPass: String, btn: Button) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Sesi habis, silakan login ulang.", Toast.LENGTH_LONG).show()
            btn.isEnabled = true
            return
        }

        // Minta user masukkan password lama untuk konfirmasi
        val etCurrentPass = EditText(this).apply {
            hint = "Masukkan password saat ini"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(40, 20, 40, 20)
        }

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Password Lama")
            .setMessage("Untuk keamanan, masukkan password saat ini:")
            .setView(etCurrentPass)
            .setPositiveButton("Konfirmasi") { _, _ ->
                val currentPass = etCurrentPass.text.toString().trim()
                if (currentPass.isEmpty()) {
                    Toast.makeText(this, "Password lama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    btn.isEnabled = true
                    return@setPositiveButton
                }

                val email = user.email ?: ""
                val credential = EmailAuthProvider.getCredential(email, currentPass)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        // Re-autentikasi berhasil, baru update password
                        user.updatePassword(newPass)
                            .addOnSuccessListener {
                                finishWithSuccess(btn)
                            }
                            .addOnFailureListener { e ->
                                btn.isEnabled = true
                                Toast.makeText(this,
                                    "Gagal update password: ${e.message}",
                                    Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        btn.isEnabled = true
                        Toast.makeText(this,
                            "Password lama salah: ${e.message}",
                            Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Lewati") { _, _ ->
                // User pilih tidak ubah password, lanjut simpan data saja
                finishWithSuccess(btn)
            }
            .setOnCancelListener { btn.isEnabled = true }
            .show()
    }

    private fun finishWithSuccess(btn: Button) {
        btn.isEnabled = true
        Toast.makeText(this, "Perubahan berhasil disimpan!", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
