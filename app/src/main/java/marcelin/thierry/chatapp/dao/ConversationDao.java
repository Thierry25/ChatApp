package marcelin.thierry.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import marcelin.thierry.chatapp.dto.Conversation;
import marcelin.thierry.chatapp.repository.IDatabaseProvider;

@Dao
public interface ConversationDao extends IDatabaseProvider<Conversation> {
    @Query("SELECT * FROM conversations")
    @Override
    LiveData<List<Conversation>> get();

    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    LiveData<Conversation> getOneById(String conversationId);

    @Insert
    @Override
    void save(Conversation conversation);

    @Update
    @Override
    void update(Conversation conversation);

    @Delete
    @Override
    void delete(Conversation conversation);
}