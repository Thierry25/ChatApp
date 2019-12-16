package marcelin.thierry.chatapp.persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import marcelin.thierry.chatapp.dao.ConversationAndMessageDao;
import marcelin.thierry.chatapp.dao.ConversationDao;
import marcelin.thierry.chatapp.dao.MessageDao;
import marcelin.thierry.chatapp.dto.Conversation;
import marcelin.thierry.chatapp.dto.ConversationAndMessage;
import marcelin.thierry.chatapp.dto.Message;

@Database(entities = {Conversation.class, Message.class, ConversationAndMessage.class}, version = 1)
public abstract class ConversationsDatabase extends RoomDatabase {

    private static final String DB_NAME = "ads_conversation.db";
    private static volatile ConversationsDatabase INSTANCE;
    public abstract ConversationDao conversationDao();
    public abstract MessageDao messageDao();
    public abstract ConversationAndMessageDao conversationAndMessageDao();

    public static ConversationsDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ConversationsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ConversationsDatabase.class, DB_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
