package marcelin.thierry.chatapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dto.ConversationAndUnreadMessage;
import marcelin.thierry.chatapp.repository.LocalUnreadMessagesRepo;

public class UnreadMessagesViewModel extends AndroidViewModel {
    private LocalUnreadMessagesRepo localUnreadMessagesRepo;
    private LiveData<List<ConversationAndUnreadMessage>> unreadMessageList;

    public UnreadMessagesViewModel(@NonNull Application application) {
        super(application);
        localUnreadMessagesRepo = new LocalUnreadMessagesRepo(application);
        unreadMessageList = localUnreadMessagesRepo.getAll();
    }

    public LiveData<ConversationAndUnreadMessage> getMessagesForConversation(String id) {
        return localUnreadMessagesRepo.getOne(id);
    }

    public LiveData<List<ConversationAndUnreadMessage>> get() {
        return unreadMessageList;
    }
}
