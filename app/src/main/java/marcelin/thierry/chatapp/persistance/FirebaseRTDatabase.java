package marcelin.thierry.chatapp.persistance;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseRTDatabase {

    private static final FirebaseAuth F_AUTH = FirebaseAuth.getInstance();
    private static final DatabaseReference USER_DB = FirebaseDatabase.getInstance()
                                                    .getReference().child("ads_users");
    private static final DatabaseReference MESSAGE_DB = FirebaseDatabase.getInstance()
                                                    .getReference().child("ads_messages");
    private static final DatabaseReference CHANNEL_MSG_DB = FirebaseDatabase.getInstance()
                                                    .getReference().child("ads_channel_messages");
    private static final DatabaseReference GROUP_MSG_DB = FirebaseDatabase.getInstance()
                                                    .getReference().child("ads_group_messages");
    private static final DatabaseReference CHAT_DB = FirebaseDatabase.getInstance()
                                                    .getReference().child("ads_chat");
    private static final DatabaseReference GROUP_DB = FirebaseDatabase.getInstance()
                                                    .getReference().child("ads_group");
    private static final DatabaseReference CHANNEL_DB = FirebaseDatabase.getInstance()
                                                    .getReference().child("ads_channel");
    private static final DatabaseReference STATUS_DB = FirebaseDatabase.getInstance()
                                                    .getReference().child("ads_status");
    private static final DatabaseReference EXCEPT_DB = FirebaseDatabase.getInstance()
                                                    .getReference().child("ads_except");

    private static final StorageReference VIDEO_STORAGE = FirebaseStorage.getInstance()
                                                    .getReference();

    public static FirebaseAuth getAuth() { return F_AUTH; }

    public static DatabaseReference getUserDb() { return USER_DB; }

    public static DatabaseReference getMessageDb() { return MESSAGE_DB; }

    public static DatabaseReference getChannelMsgDb() { return CHANNEL_MSG_DB; }

    public static DatabaseReference getGroupMsgDb() { return GROUP_MSG_DB; }

    public static DatabaseReference getChatDb() { return CHAT_DB; }

    public static DatabaseReference getGroupDb() { return GROUP_DB; }

    public static DatabaseReference getChannelDb() { return CHANNEL_DB; }

    public static DatabaseReference getStatusDb() { return STATUS_DB; }

    public static DatabaseReference getExceptDb() { return EXCEPT_DB; }

    public static StorageReference getVideoStorage() { return VIDEO_STORAGE; }
}
