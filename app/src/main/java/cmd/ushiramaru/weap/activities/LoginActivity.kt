package cmd.ushiramaru.weap.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import cmd.ushiramaru.weap.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.edt_email
import kotlinx.android.synthetic.main.activity_login.edt_password
import kotlinx.android.synthetic.main.activity_login.progress_layout
import kotlinx.android.synthetic.main.activity_login.til_email
import kotlinx.android.synthetic.main.activity_login.til_password
import kotlinx.android.synthetic.main.activity_signup.*

class LoginActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        // mengecheck userId yang sedang aktif, jika ada, proses akan langsung intent ke hal.utama
        val user = firebaseAuth.currentUser?.uid
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) // menghilangkan ActionBar
        setContentView(R.layout.activity_login)

        setTextChangedListener(edt_email, til_email)
        setTextChangedListener(edt_password, til_password)
        progress_layout.setOnTouchListener { _ , _ -> true }

        btn_login.setOnClickListener {
            onLogin()
        }

        txt_signup.setOnClickListener {
            onSignup()
        }
    }

    //Region Login Email
    private fun setTextChangedListener(edt: EditText, til: TextInputLayout) {
        edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }
            override fun beforeTextChanged(s: CharSequence? , start: Int, count: Int, after: Int) {

            }
            // ketika editText diubah memastikan TextInputLayout tidak menunjukkan pesan error
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled = false
            }
        })
    }

    //region Login
    private fun onLogin() {
        var proceed = true
        if (edt_email.text.isNullOrEmpty()) {
            til_email.error = "Required Email"
            til_email.isErrorEnabled = true
            proceed = false
        }
        if (edt_password.text.isNullOrEmpty()) {
            til_password.error = "Required Password"
            til_password.isErrorEnabled = true
            proceed = false
        }

        if (proceed) {
            progress_layout.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(
                edt_email.text.toString(),
                edt_password.text.toString()
            )
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        progress_layout.visibility = View.GONE
                        Toast.makeText(
                            this@LoginActivity,
                            "Login error: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    progress_layout.visibility = View.GONE
                    e.printStackTrace()
                }
        }
    }

    private fun onSignup() {
        startActivity(Intent(this, SignupActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart() // method yang pertama kali dijalankan sebelum method lainnya
         firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop() // dijalankan jika proses dalam activity selesai atau dihentikan system
         firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

}
