package marcelin.thierry.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import marcelin.thierry.chatapp.dto.Contact;
import marcelin.thierry.chatapp.repository.IDatabaseProvider;

@Dao
public interface ContactDao extends IDatabaseProvider<Contact> {

    @Query("SELECT * FROM contacts")
    @Override
    LiveData<List<Contact>> get();

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber")
    Contact getOneByPhone(String phoneNumber);

    @Query("SELECT * FROM contacts WHERE contactName = :contactName")
    Contact getOneByName(String contactName);

    @Insert
    @Override
    void save(Contact contact);

    @Update
    @Override
    void update(Contact contact);

    @Delete
    @Override
    void delete(Contact contact);


}
