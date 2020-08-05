package cmd.ushiramaru.weap.listeners

interface ChatClickListener {
    fun onChatClicked(name: String?, otherUserId: String?, chatsImageUrl: String?,
                      chatsName: String?)
}