package marcelin.thierry.chatapp.viewmodel;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dao.ContactDao;
import marcelin.thierry.chatapp.dto.Contact;
import marcelin.thierry.chatapp.persistance.ContactsDatabase;

public class ContactViewModel extends AndroidViewModel {
    private ContactDao contactDao;
    private final InsertAsyncTask insertAsyncTask = new InsertAsyncTask();
    private final UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask();

    public ContactViewModel(@NonNull Application application) {
        super(application);

        ContactsDatabase contactsDb = ContactsDatabase.getInstance(application);
        contactDao = contactsDb.contactDao();
    }

    public LiveData<List<Contact>> getPhoneUsers() {
        return contactDao.get();
    }

    public Contact getPhoneUserByName(String contactName) {
        return contactDao.getOneByName(contactName);
    }

    public Contact getPhoneUserByNumber(String number) {
        return contactDao.getOneByPhone(number);
    }

    public void insertContact(Contact contact) {
        insertAsyncTask.execute(contact);
    }

    public void updateContact(Contact contact) {
        updateAsyncTask.execute(contact);
    }

    private class InsertAsyncTask extends AsyncTask<Contact, Void, Void> {

        @Override
        protected Void doInBackground(Contact... contacts) {
            contactDao.save(contacts[0]);
            return null;
        }
    }

    private class UpdateAsyncTask extends AsyncTask<Contact, Void, Void> {
        @Override
        protected Void doInBackground(Contact... contacts) {
            contactDao.update(contacts[0]);
            return null;
        }
    }
}
