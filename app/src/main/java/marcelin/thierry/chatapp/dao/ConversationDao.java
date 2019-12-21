package marcelin.thierry.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import marcelin.thierry.chatapp.dto.Conversation;

@Dao
public interface ConversationDao {
    @Query("SELECT * FROM conversations")
    LiveData<List<Conversation>> get();

    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    LiveData<Conversation> getOneById(String conversationId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void save(Conversation conversation);

    @Update
    void update(Conversation conversation);

    @Delete
    void delete(Conversation conversation);
}