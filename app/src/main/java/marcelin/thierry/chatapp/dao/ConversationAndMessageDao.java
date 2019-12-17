package marcelin.thierry.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import marcelin.thierry.chatapp.dto.ConversationAndMessage;
import marcelin.thierry.chatapp.repository.IDatabaseProvider;

@Dao
public interface ConversationAndMessageDao extends IDatabaseProvider<ConversationAndMessage> {
    @Transaction
    @Query("SELECT * FROM conversations")
    @Override
    LiveData<List<ConversationAndMessage>> get();

    @Transaction
    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    LiveData<ConversationAndMessage> getOneById(String conversationId);
}
