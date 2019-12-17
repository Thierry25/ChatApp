package marcelin.thierry.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import marcelin.thierry.chatapp.dto.ConversationAndLastMessage;
import marcelin.thierry.chatapp.repository.IDatabaseProvider;

@Dao
public interface ConversationAndLastMessageDao extends IDatabaseProvider<ConversationAndLastMessage> {
    @Transaction
    @Query("SELECT * FROM conversations")
    @Override
    LiveData<List<ConversationAndLastMessage>> get();

    @Transaction
    @Query("SELECT * FROM conversations where conversationId = :conversationId")
    LiveData<ConversationAndLastMessage> getOneById(String conversationId);
}
