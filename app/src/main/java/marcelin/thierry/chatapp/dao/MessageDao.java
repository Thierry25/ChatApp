package marcelin.thierry.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import marcelin.thierry.chatapp.dto.Message;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversation = :conversationId")
    LiveData<List<Message>> getAllByConversationId(String conversationId);

    @Query("SELECT * FROM messages WHERE messageTimestamp = " +
            "(SELECT MAX(messageTimestamp) FROM messages WHERE conversation = :conversationId) " +
            "ORDER BY messageId DESC LIMIT 1")
    LiveData<Message> getLastMessageForConversation(String conversationId);

    @Query("SELECT * FROM messages WHERE conversation = :conversationId " +
            "AND seen = 0")
    LiveData<List<Message>> getUnseenMessageCountForConversation(String conversationId);

    @Query("SELECT * FROM messages")
    LiveData<List<Message>> get();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void save(Message message);

    @Delete
    void delete(Message message);

    @Update
    void update(Message message);
}
