package idn.project_whatsapp.whatsapp.listeners

import idn.project_whatsapp.whatsapp.utils.StatusListElement

interface ProgressListener {
    fun onProgressUpdate(progress: Int)
}

interface ChatClickListener {
    fun onChatClicked(name: String?, otherUserId: String?, chatsImageUrl: String?,
                      chatsName: String?)
}

interface ContactsClickListener {
    fun onContactClicked(name: String?, phone: String?)
}

interface FailureCallback {
    fun userError()
}

interface StatusItemClickListener {
    fun onItemClicked(statusElement: StatusListElement)
}