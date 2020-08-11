package idn.project_whatsapp.whatsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import idn.project_whatsapp.whatsapp.R
import idn.project_whatsapp.whatsapp.listeners.ContactsClickListener
import idn.project_whatsapp.whatsapp.utils.Contact
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contact.view.*

class ContactsAdapter(val contacts: ArrayList<Contact>) : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    private var clickListener: ContactsClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ContactsViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_contact,
            parent, false
        )
    )

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bindItem(contacts[position], clickListener)
    }

    class ContactsViewHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView!!), LayoutContainer {
        fun bindItem(contact: Contact, listener: ContactsClickListener?) {
            containerView?.txt_contact_name!!.text = contact.name
            containerView.txt_contact_number.text = contact.phone
            itemView.setOnClickListener {
                listener?.onContactClicked(contact.name, contact.phone) }
        }
    }

    fun setOnItemClickListener(listener: ContactsClickListener) { clickListener = listener
        notifyDataSetChanged()
    }
}
