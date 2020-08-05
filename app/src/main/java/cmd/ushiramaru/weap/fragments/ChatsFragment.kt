package cmd.ushiramaru.weap.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cmd.ushiramaru.weap.R
import cmd.ushiramaru.weap.adapters.ChatsAdapter
import cmd.ushiramaru.weap.listeners.ChatClickListener
import cmd.ushiramaru.weap.listeners.FailureCallback
import cmd.ushiramaru.weap.utils.Chat
import cmd.ushiramaru.weap.utils.Constants.DATA_CHATS
import cmd.ushiramaru.weap.utils.Constants.DATA_USERS
import cmd.ushiramaru.weap.utils.Constants.DATA_USER_CHATS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_chats.*

/**
 * A simple [Fragment] subclass.
 */
class ChatsFragment : Fragment(), ChatClickListener {

    private var chatsAdapter = ChatsAdapter(arrayListOf())

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private var failureCallback: FailureCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (userId.isNullOrEmpty()) {
            failureCallback?.userError()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatsAdapter.setOnItemClickListener(this)
        rv_chats.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = chatsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        firebaseDb.collection(DATA_USERS).document(userId!!)
            .addSnapshotListener { _, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                refreshChats()
                }
            }
    }


    override fun onChatClicked(
        name: String?,
        otherUserId: String?,
        chatsImageUrl: String?,
        chatsName: String?
    ) {
        Toast.makeText(context, "$name clicked", Toast.LENGTH_SHORT).show()
    }

    fun newChat(partnerId: String) {
        firebaseDb.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener { userDocument ->
                val userChatPartners = hashMapOf<String, String>()
                if (userDocument[DATA_USER_CHATS] != null && userDocument[DATA_USER_CHATS] is HashMap<*, *>) {
                    val userDocumentMap = userDocument[DATA_USER_CHATS] as HashMap<String, String>
                    if (userDocumentMap.containsKey(partnerId)) {
                        return@addOnSuccessListener
                    } else {
                        userChatPartners.putAll(userDocumentMap)
                    }
                }

                firebaseDb.collection(DATA_USERS).document(partnerId)
                    .get()
                    .addOnSuccessListener { partnerDocument ->
                        val partnerChatPartners = hashMapOf<String, String>()
                        if (partnerDocument[DATA_USER_CHATS] != null &&
                            partnerDocument[DATA_USER_CHATS] is HashMap<*, *>
                        ) {
                            val partnerDocumentMap =
                                partnerDocument[DATA_USER_CHATS] as HashMap<String, String>
                            partnerChatPartners.putAll(partnerDocumentMap)
                        }

                        val chatParticipants = arrayListOf(userId, partnerId)
                        val chat = Chat(chatParticipants)
                        val chatRef = firebaseDb.collection(DATA_CHATS).document()
                        val userRef = firebaseDb.collection(DATA_USERS).document(userId)
                        val partnerRef = firebaseDb.collection(DATA_USERS).document(partnerId)
                        userChatPartners[partnerId] = chatRef.id
                        partnerChatPartners[userId] = chatRef.id

                        val batch = firebaseDb.batch()
                        batch.set(chatRef, chat)
                        batch.update(userRef, DATA_USER_CHATS, userChatPartners)
                        batch.update(partnerRef, DATA_USER_CHATS, partnerChatPartners)
                        batch.commit()
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }

            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun setFailureCallbackListener(listener: FailureCallback) {
        failureCallback = listener
    }

    private fun refreshChats() {
        firebaseDb.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener {
                if (it.contains(DATA_USER_CHATS)) {
                    val partners = it[DATA_USER_CHATS]
                    val chats = arrayListOf<String>()
                    for (partner in (partners as HashMap<String, String>).keys) {
                        if (partners[partner] != null) {
                            chats.add(partners[partner]!!)
                        }
                    }
                    chatsAdapter.updateChats(chats)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
