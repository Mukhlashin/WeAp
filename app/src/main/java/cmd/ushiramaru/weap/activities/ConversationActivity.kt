package cmd.ushiramaru.weap.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import cmd.ushiramaru.weap.R
import cmd.ushiramaru.weap.adapters.ConversationAdapter
import cmd.ushiramaru.weap.utils.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private var chatId: String? = null
    private var imageUrl: String? = null
    private var otherUserId: String? = null
    private var chatName: String? = null

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)

    companion object {
        private val PARAM_CHAT_ID = "Chat_id"
        private val PARAM_IMAGE_URL = "Image_url"
        private val PARAM_OTHER_USER_ID = "Other_user_id"
        private val PARAM_CHAT_NAME = "Chat_name"
        fun newIntent(context: Context?, chatId: String?, imageUrl: String?, otherUserId: String?, chatName: String?): Intent {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(PARAM_CHAT_ID, chatId)
            intent.putExtra(PARAM_IMAGE_URL, imageUrl)
            intent.putExtra(PARAM_OTHER_USER_ID, otherUserId)
            intent.putExtra(PARAM_CHAT_NAME, chatName)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        rv_message.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        conversationAdapter.addMessage(
            Message(userId, "Hello", 2))
        conversationAdapter.addMessage(Message("everytime", "How are you?", 3))
        conversationAdapter.addMessage(Message(userId, "I'm good, how are you?", 4))
        conversationAdapter.addMessage(Message("everytime", "Me too", 5))
        btn_send.setOnClickListener {
        }
    }
}
