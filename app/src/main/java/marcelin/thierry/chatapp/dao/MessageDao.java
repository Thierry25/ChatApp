package marcelin.thierry.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import marcelin.thierry.chatapp.dto.Message;
import marcelin.thierry.chatapp.repository.IDatabaseProvider;

@Dao
public interface MessageDao extends IDatabaseProvider<Message> {
    @Query("SELECT * FROM messages where conversation = :conversationId")
    LiveData<List<Message>> getAllByConversationId(String conversationId);

    @Query("SELECT * FROM messages where messageTimestamp = " +
            "(SELECT MAX(messageTimestamp) FROM messages WHERE conversation = :conversationId)" +
            "ORDER BY messageId DESC LIMIT 1")
    LiveData<Message> getLastMessageForConversation(String conversationId);

    @Query("SELECT SUM(messageId) FROM messages where conversation = :conversationId")
    LiveData<Integer> getUnseenMessageCountForConversation(String conversationId);

    @Insert
    @Override
    void save(Message message);

    @Update
    @Override
    void update(Message message);
}
