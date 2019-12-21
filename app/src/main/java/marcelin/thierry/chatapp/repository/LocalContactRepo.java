package marcelin.thierry.chatapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dao.ContactDao;
import marcelin.thierry.chatapp.dto.Contact;
import marcelin.thierry.chatapp.persistance.ContactsDatabase;

public class LocalContactRepo implements IDatabaseProvider<Contact> {
    private ContactsDatabase db;
    private ContactDao contactDao;
    private LiveData<List<Contact>> contactList;

    public LocalContactRepo(Application application) {
        db = ContactsDatabase.getInstance(application);
        contactDao = db.contactDao();
        contactList = contactDao.get();
    }

    @Override
    public LiveData<List<Contact>> getAll() {
        return contactList;
    }

    //TODO: [fm] remove function not defined in interface
    public Contact getOneByName(String name) {
        return contactDao.getOneByName(name);
    }

    //TODO: [fm] remove function not defined in interface
    public Contact getOneByPhone(String phone) {
        return contactDao.getOneByPhone(phone);
    }

    @Override
    public LiveData<List<Contact>> getAllById(String id) {
        return null;
    }

    @Override
    public LiveData<List<Contact>> getAllForParam(String param, String id) {
        return null;
    }

    @Override
    public LiveData<Contact> getOne(String id) {
        return null;
    }

    @Override
    public LiveData<Contact> getOne() {
        return null;
    }

    @Override
    public LiveData<Contact> getOneByParam(String param, String id) {
        return null;
    }

    @Override
    public void save(Contact contact) {
        ContactsDatabase.databaseWriteExecutor.execute(() -> contactDao.save(contact));
    }

    @Override
    public void update(Contact contact) {
        ContactsDatabase.databaseWriteExecutor.execute(() -> contactDao.update(contact));
    }

    @Override
    public void delete(Contact contact) {
        ContactsDatabase.databaseWriteExecutor.execute(() -> contactDao.delete(contact));
    }
}
