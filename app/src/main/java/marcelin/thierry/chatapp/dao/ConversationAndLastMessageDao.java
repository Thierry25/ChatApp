package marcelin.thierry.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import marcelin.thierry.chatapp.dto.ConversationAndLastMessage;

@Dao
public interface ConversationAndLastMessageDao {
    @Query("SELECT conversations.*, messageContent, messageTimestamp, seen FROM conversations " +
            "LEFT JOIN messages ON conversations.lastMessageId = messages.messageId")
    LiveData<List<ConversationAndLastMessage>> getAll();

    @Query("SELECT conversations.*, messageContent, messageTimestamp, seen FROM conversations " +
            "LEFT JOIN messages ON conversations.lastMessageId = messages.messageId " +
            "AND conversations.conversationId = :id")
    LiveData<List<ConversationAndLastMessage>> getAllById(String id);
}
