package marcelin.thierry.chatapp.persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import marcelin.thierry.chatapp.dao.ConversationAndLastMessageDao;
import marcelin.thierry.chatapp.dao.ConversationAndUnreadMessageDao;
import marcelin.thierry.chatapp.dao.ConversationDao;
import marcelin.thierry.chatapp.dao.MessageDao;
import marcelin.thierry.chatapp.dto.Conversation;
import marcelin.thierry.chatapp.dto.Message;
import marcelin.thierry.chatapp.dto.UnreadMessages;

@Database(entities = {Conversation.class, Message.class}, views = {UnreadMessages.class},
        version = 1, exportSchema = false)
public abstract class ConversationsDatabase extends RoomDatabase {

    private static final String DB_NAME = "ads_conversation.db";
    private static volatile ConversationsDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public abstract ConversationDao conversationDao();
    public abstract MessageDao messageDao();
    public abstract ConversationAndUnreadMessageDao conversationAndUnreadMessageDao();
    public abstract ConversationAndLastMessageDao conversationAndLastMessageDao();

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

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
