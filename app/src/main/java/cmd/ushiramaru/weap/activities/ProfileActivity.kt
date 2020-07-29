package cmd.ushiramaru.weap.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cmd.ushiramaru.weap.R
import cmd.ushiramaru.weap.utils.Constants.DATA_IMAGES
import cmd.ushiramaru.weap.utils.Constants.DATA_USERS
import cmd.ushiramaru.weap.utils.Constants.DATA_USER_EMAIL
import cmd.ushiramaru.weap.utils.Constants.DATA_USER_IMAGE_URL
import cmd.ushiramaru.weap.utils.Constants.DATA_USER_NAME
import cmd.ushiramaru.weap.utils.Constants.DATA_USER_PHONE
import cmd.ushiramaru.weap.utils.Constants.REQUEST_CODE_PHOTO
import cmd.ushiramaru.weap.utils.User
import cmd.ushiramaru.weap.utils.populateImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity : AppCompatActivity() {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference               // mengakses firebase
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imbtn_profile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PHOTO)

            if (userId.isNullOrEmpty()) {
                finish()                    // jika userId null, ProfileActivity akan dihentikan finish() dan kembali ke MainActivity
            }
        }

        progress_layout.setOnTouchListener { v, event -> true }
        btn_apply.setOnClickListener {
            onApply()
        }

        btn_delete_account.setOnClickListener {
            onDelete()
        }
        populateInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storeImage(data?.data)                                                                          // method storeImage dijalankan setelah pengguna memilih gambar }
        }
    }

    private fun populateInfo() {
        progress_layout.visibility = View.VISIBLE
        firebaseDb.collection(DATA_USERS)
            .document(userId!!)
            .get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                edt_name_profile.setText(user?.name, TextView.BufferType.EDITABLE)
                edt_email_profile.setText(user?.email, TextView.BufferType.EDITABLE)
                edt_phone_profile.setText(user?.phone, TextView.BufferType.EDITABLE)
                progress_layout.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    fun onApply() {
        progress_layout.visibility = View.VISIBLE
        val name = edt_name_profile.text.toString()
        val email = edt_email_profile.text.toString()
        val phone = edt_phone_profile.text.toString()
        val map = HashMap<String, Any>()

        map[DATA_USER_NAME] = name
        map[DATA_USER_EMAIL] = email
        map[DATA_USER_PHONE] = phone

        firebaseDb.collection(DATA_USERS).document(userId!!).update(map) // perintah update
            .addOnSuccessListener {
                Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                progress_layout.visibility = View.GONE
            }

        firebaseDb.collection(DATA_USERS)
            .document(userId!!)
            .update(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "Update Failed",
                    Toast.LENGTH_SHORT
                ).show()
                progress_layout.visibility = View.GONE
            }

    }

    private fun onDelete() {
        progress_layout.visibility = View.VISIBLE
        val input = EditText(this@ProfileActivity)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        AlertDialog.Builder(this)                                                             // ketika tombol DELETE diklik, AlertDialog akan muncul
            .setTitle("Delete Account")                                                              // Title AlertDialog
            .setMessage("This will delete your Profile Information. Are you sure?")
            .setView(input)
            .setPositiveButton("Yes") { dialog, which->
                DialogInterface.OnClickListener { dialog, which ->
                    firebaseDb.collection(DATA_USERS)
                        .document(userId!!)
                        .get()
                        .addOnSuccessListener {
                            val user = it.toObject(User::class.java)
                            val password = user?.password
                            if (password!!.compareTo(password) == 0) {
                                if (input.equals(password)) {
                                    Toast.makeText(applicationContext,
                                        "Password Matched", Toast.LENGTH_SHORT).show();
                                    firebaseDb.collection(DATA_USERS).document(userId!!).delete()                                // perintah delete
                                    Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show()
                                    startActivityForResult(Intent(this, LoginActivity::class.java), 0)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            progress_layout.visibility = View.GONE
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            finish()
                        }
                }
            }.setNegativeButton("No") { dialog, which ->
                progress_layout.visibility = View.GONE
            }
            .setCancelable(false)                                                                           // AlertDialog tidak dapat hilang kecuali menekan buton Yes/No
            .show()                                                                                         // memunculkan AlertDialog }
    }

    private fun storeImage(uri: Uri?) {
        if (uri != null) {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
            progress_layout.visibility = View.VISIBLE
            val filePath = firebaseStorage.child(DATA_IMAGES).child(userId!!) // membuat folder
            filePath.putFile(uri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener {
                            val url = it.toString()
                            firebaseDb.collection(DATA_USERS).document(userId)
                                .update(DATA_USER_IMAGE_URL, url).addOnSuccessListener {
                                    imageUrl = url
                                    populateImage(this, imageUrl, img_profile, R.drawable.ic_user)
                                }
                        }
                    progress_layout.visibility = View.GONE
                }
            }
        }
    }
