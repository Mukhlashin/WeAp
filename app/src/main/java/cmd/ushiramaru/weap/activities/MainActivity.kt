package cmd.ushiramaru.weap.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cmd.ushiramaru.weap.R
import cmd.ushiramaru.weap.adapters.SectionPagerAdapter
import cmd.ushiramaru.weap.fragments.ChatsFragment
import cmd.ushiramaru.weap.listeners.FailureCallback
import cmd.ushiramaru.weap.utils.Constants.DATA_USERS
import cmd.ushiramaru.weap.utils.Constants.DATA_USERS_PHONE
import cmd.ushiramaru.weap.utils.Constants.PERMISSION_REQUEST_READ_CONTACT
import cmd.ushiramaru.weap.utils.Constants.REQUEST_NEW_CHATS
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainActivity : AppCompatActivity(), FailureCallback {
    // Deklarasikan pada MainActivity
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var mSectionPagerAdapter: SectionPagerAdapter? = null
    private val chatsFragment = ChatsFragment()

    companion object {
        const val PARAM_NAME = "name"
        const val PARAM_PHONE = "phone"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chatsFragment.setFailureCallbackListener(this)

        setSupportActionBar(toolbar)
        mSectionPagerAdapter = SectionPagerAdapter(
            supportFragmentManager
        )

        container.adapter = mSectionPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
//        resizeTabs()
        tabs.getTabAt(1)?.select()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> fab.hide()
                    1 -> fab.show()
                    2 -> fab.hide()
                }
            }

        })

        fab.setOnClickListener {
            onNewChat()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_READ_CONTACT -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    startNewActivity()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_logout -> onLogout()
            R.id.action_profile -> onProfile()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    private fun onLogout() {
        firebaseAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    class PlaceHolderFragment : Fragment() {
        companion object {
            private val ARG_SECTION_NUMBER = "Section number"
            fun newIntent(sectionNumber: Int): PlaceHolderFragment {
                val fragment = PlaceHolderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            rootView.section_label.text =
                "Hello world, from section ${arguments?.getInt(ARG_SECTION_NUMBER)}"
            return rootView
        }
    }

    private fun onNewChat() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Contacts Permission")
                    .setMessage("This App Requires Access to Your Contacts to Initialize A Conversation")
                    .setPositiveButton("Yes") { _, _ ->
                        requestContactPermission()
                    }
                    .setNegativeButton("No") { _, _ -> }
                    .show()
            } else {
                requestContactPermission()
            }
        } else {
            startNewActivity()
        }
    }

    private fun startNewActivity() {
        startActivityForResult(Intent(this, ContactActivity::class.java), REQUEST_NEW_CHATS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_NEW_CHATS -> {
                    val name = data?.getStringExtra(PARAM_NAME) ?: ""
                    val phone = data?.getStringExtra(PARAM_PHONE) ?: ""
                    checkNewChatUser(name, phone)
                }
            }
        }
    }

    private fun checkNewChatUser(name: String, phone: String) {
        if (!name.isNullOrEmpty() && !phone.isNullOrEmpty()) {
            firebaseDb.collection(DATA_USERS)
                .whereEqualTo(DATA_USERS_PHONE, phone)
                .get()
                .addOnSuccessListener {
                    if (it.documents.size > 0) {
                        chatsFragment.newChat(it.documents[0].id)
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("User not found")
                            .setMessage("$name does not have an account. Send them an SMS to install this app.")
                            .setPositiveButton("OK") { dialog, which ->
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data =
                                    Uri.parse("sms:$phone") // query untuk mengirim pesan intent
                                intent.putExtra(
                                    "sms_body",
                                    "Hi I'm using this new cool WhatsAppClone app. You should install it too so we can chat there."
                                )
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancel", null)
                            .setCancelable(false)
                            .show()
                    }
                }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "An error occured. Please try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                            e.printStackTrace()
                        }
        }
    }
    private fun requestContactPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            PERMISSION_REQUEST_READ_CONTACT
        )
    }

    override fun userError() {
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}