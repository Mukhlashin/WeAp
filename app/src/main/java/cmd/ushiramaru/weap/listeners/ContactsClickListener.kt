package cmd.ushiramaru.weap.listeners

interface ContactsClickListener {
    fun onContactClicked(name: String?, phone: String?)
}