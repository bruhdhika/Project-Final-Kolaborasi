package com.example.e_ntog

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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

    // Launcher buka galeri
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            val imgAvatar = findViewById<CircleImageView>(R.id.edit_img_avatar)
            Glide.with(this).load(it).circleCrop().into(imgAvatar)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data)


        setupBackButton()

        session = SessionManager(this)

        val etNama          = findViewById<EditText>(R.id.et_edit_nama)
        val etEmail         = findViewById<EditText>(R.id.et_edit_email)
        val etPassword      = findViewById<EditText>(R.id.et_edit_password)
        val etConfirm       = findViewById<EditText>(R.id.et_confirm_password)
        val layoutConfirm   = findViewById<android.widget.LinearLayout>(R.id.layout_konfirmasi_pass)
        val btnSimpan       = findViewById<Button>(R.id.btn_simpan_perubahan)
        val btnChangePhoto  = findViewById<ImageButton>(R.id.btn_change_photo)
        val imgAvatar       = findViewById<CircleImageView>(R.id.edit_img_avatar)

        // Load data saat ini
        val uid = session.getUid()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                etNama.setText(snap.getString("nama") ?: "")
                etEmail.setText(snap.getString("email") ?: "")
                val photoUrl = snap.getString("photoUrl") ?: ""
                if (photoUrl.isNotEmpty())
                    Glide.with(this).load(photoUrl).circleCrop().into(imgAvatar)
            }

        // Tampilkan konfirmasi password jika user mulai isi password baru
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                layoutConfirm.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Tombol ganti foto — buka galeri
        btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Tombol simpan
        btnSimpan.setOnClickListener {
            val namaBaru   = etNama.text.toString().trim()
            val emailBaru  = etEmail.text.toString().trim()
            val passBaru   = etPassword.text.toString().trim()
            val passConfirm = etConfirm.text.toString().trim()

            if (namaBaru.isEmpty()) { etNama.error = "Nama tidak boleh kosong"; return@setOnClickListener }

            // Jika ada password baru
            if (passBaru.isNotEmpty()) {
                if (passBaru.length < 6) { etPassword.error = "Minimal 6 karakter"; return@setOnClickListener }
                if (passBaru != passConfirm) { etConfirm.error = "Password tidak cocok"; return@setOnClickListener }
            }

            btnSimpan.isEnabled = false

            // Jika ada foto baru yang dipilih, upload dulu
            if (selectedImageUri != null) {
                uploadFotoThenSave(uid, namaBaru, emailBaru, passBaru, btnSimpan)
            } else {
                saveData(uid, namaBaru, emailBaru, passBaru, null, btnSimpan)
            }
        }
    }

    private fun uploadFotoThenSave(
        uid: String, nama: String, email: String, pass: String,
        btnSimpan: Button
    ) {
        val ref = storage.reference.child("profile_photos/$uid.jpg")
        ref.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveData(uid, nama, email, pass, uri.toString(), btnSimpan)
                }
            }
            .addOnFailureListener {
                btnSimpan.isEnabled = true
                Toast.makeText(this, "Gagal upload foto: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveData(
        uid: String, nama: String, email: String, pass: String,
        photoUrl: String?, btnSimpan: Button
    ) {
        val updates = mutableMapOf<String, Any>("nama" to nama)
        if (!photoUrl.isNullOrEmpty()) updates["photoUrl"] = photoUrl

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                session.updateNama(nama)

                // Jika ada password baru, update juga
                if (pass.isNotEmpty()) {
                    auth.currentUser?.updatePassword(pass)
                        ?.addOnSuccessListener {
                            finishWithSuccess()
                        }
                        ?.addOnFailureListener {
                            // Token mungkin kadaluarsa, arahkan untuk re-login
                            Toast.makeText(this,
                                "Data tersimpan. Untuk ubah password, silakan logout & login kembali.",
                                Toast.LENGTH_LONG).show()
                            finishWithSuccess()
                        }
                } else {
                    finishWithSuccess()
                }
            }
            .addOnFailureListener {
                btnSimpan.isEnabled = true
                Toast.makeText(this, "Gagal simpan: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun finishWithSuccess() {
        Toast.makeText(this, "Perubahan berhasil disimpan!", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
