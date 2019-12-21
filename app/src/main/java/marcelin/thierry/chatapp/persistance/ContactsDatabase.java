package marcelin.thierry.chatapp.persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import marcelin.thierry.chatapp.dao.ContactDao;
import marcelin.thierry.chatapp.dto.Contact;

@Database(entities = Contact.class, version = 1, exportSchema = false)
public abstract class ContactsDatabase extends RoomDatabase {

    private static final String DB_NAME = "ads_contact.db";
    private static final int NUMBER_OF_THREADS = 4;
    private static volatile ContactsDatabase INSTANCE;
    public abstract ContactDao contactDao();

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static ContactsDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ContactsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ContactsDatabase.class, DB_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}