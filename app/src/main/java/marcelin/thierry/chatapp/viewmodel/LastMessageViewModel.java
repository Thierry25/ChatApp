package marcelin.thierry.chatapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dto.ConversationAndLastMessage;
import marcelin.thierry.chatapp.repository.LocalLastMessagesRepo;

public class LastMessageViewModel extends AndroidViewModel {
    private LocalLastMessagesRepo lastMessagesRepo;

    public LastMessageViewModel(@NonNull Application application) {
        super(application);
        lastMessagesRepo = new LocalLastMessagesRepo(application);
    }

    public LiveData<List<ConversationAndLastMessage>> getAllConversationLastMessage() {
        return lastMessagesRepo.getAll();
    }

    public LiveData<List<ConversationAndLastMessage>> getLastMessageForConversation(String id) {
        return lastMessagesRepo.getAllById(id);
    }
}
