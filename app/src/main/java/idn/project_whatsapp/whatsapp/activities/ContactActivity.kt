package idn.project_whatsapp.whatsapp.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import idn.project_whatsapp.whatsapp.R
import idn.project_whatsapp.whatsapp.adapters.ContactsAdapter
import idn.project_whatsapp.whatsapp.listeners.ContactsClickListener
import idn.project_whatsapp.whatsapp.utils.Contact
import kotlinx.android.synthetic.main.activity_contact.*

class ContactActivity : AppCompatActivity(), ContactsClickListener {

    private val contactsList = ArrayList<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        getContacts()
        setupList()
    }

    private fun getContacts() {
        progress_layout.visibility = View.VISIBLE
        contactsList.clear()
        val newList = ArrayList<Contact>()
        val phone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phone!!.moveToNext()){
            val name = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            newList.add(Contact(name, phoneNumber))
        }
        contactsList.addAll(newList)
        phone.close()
    }

    private fun setupList() {
        progress_layout.visibility = View.GONE
        val contactsAdapter = ContactsAdapter(contactsList)
         contactsAdapter.setOnItemClickListener(this)
         rv_contacts.apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        adapter = contactsAdapter
        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }
}

    override fun onContactClicked(name: String?, phone: String?) {
        val intent = Intent()
        intent.putExtra(MainActivity.PARAM_NAME, name)
        intent.putExtra(MainActivity.PARAM_PHONE, phone)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
