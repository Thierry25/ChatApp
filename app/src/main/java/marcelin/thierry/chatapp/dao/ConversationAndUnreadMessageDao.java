package marcelin.thierry.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import marcelin.thierry.chatapp.dto.ConversationAndUnreadMessage;

@Dao
public interface ConversationAndUnreadMessageDao {

    @Transaction
    @Query("SELECT * FROM conversations")
    LiveData<List<ConversationAndUnreadMessage>> getAllUnreadMessages();

    @Transaction
    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    LiveData<ConversationAndUnreadMessage> getUnreadMessagesForOne(String conversationId);
}
