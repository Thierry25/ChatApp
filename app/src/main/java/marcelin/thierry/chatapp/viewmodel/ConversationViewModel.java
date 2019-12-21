package marcelin.thierry.chatapp.viewmodel;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dto.Conversation;
import marcelin.thierry.chatapp.repository.LocalConversationRepo;

public class ConversationViewModel extends AndroidViewModel {
    private LocalConversationRepo localConversationRepo;

    public ConversationViewModel(@NonNull Application application) {
        super(application);
        localConversationRepo = new LocalConversationRepo(application);
    }

    public LiveData<List<Conversation>> getAllConversations() {
        return localConversationRepo.getAll();
    }

    public LiveData<Conversation> getConversationById(String conversationId) {
        return localConversationRepo.getOne(conversationId);
    }

    public void insertConversation(Conversation conversation) {
        localConversationRepo.save(conversation);
    }

    public void deleteConversation(Conversation conversation) {
        localConversationRepo.delete(conversation);
    }

    public void updateConversation(Conversation conversation) {
        localConversationRepo.update(conversation);
    }

}
