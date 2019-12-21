package marcelin.thierry.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import marcelin.thierry.chatapp.dto.Contact;

@Dao
public interface ContactDao {

    @Query("SELECT * FROM contacts")
    LiveData<List<Contact>> get();

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber")
    Contact getOneByPhone(String phoneNumber);

    @Query("SELECT * FROM contacts WHERE contactName = :contactName")
    Contact getOneByName(String contactName);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void save(Contact contact);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);

}
