package marcelin.thierry.chatapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dto.Contact;
import marcelin.thierry.chatapp.repository.LocalContactRepo;

public class ContactViewModel extends AndroidViewModel {
    private LocalContactRepo localContactRepo;

    public ContactViewModel(@NonNull Application application) {
        super(application);
        localContactRepo = new LocalContactRepo(application);
    }

    public LiveData<List<Contact>> getPhoneUsers() {
        return localContactRepo.getAll();
    }

    public Contact getPhoneUserByName(String contactName) {
        return localContactRepo.getOneByName(contactName);
    }

    public Contact getPhoneUserByNumber(String number) {
        return localContactRepo.getOneByPhone(number);
    }

    public void insertContact(Contact contact) {
        localContactRepo.save(contact);
    }

    public void updateContact(Contact contact) {
        localContactRepo.save(contact);
    }
}
