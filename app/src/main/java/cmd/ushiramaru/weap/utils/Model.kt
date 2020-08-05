package cmd.ushiramaru.weap.utils

data class User(
    val email: String? = "",        // Model merupakan layer yang menunjuk pada objek dan
    val phone: String? = "",        // data yang ada pada aplikasi
    val name: String? = "",         // sehingga User disini akan memiliki data-data disamping
    val password: String? = "",
    val imageUrl: String? = "",
    val status: String? = "",
    val statusUrl: String? = "",
    val statusTime: String? = ""
)

data class Contact
    ( val name: String?,
      val phone: String?
)

data class Chat(
    val chatParticipants: ArrayList<String>
)