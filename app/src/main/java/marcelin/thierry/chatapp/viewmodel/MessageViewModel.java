package marcelin.thierry.chatapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dto.Message;
import marcelin.thierry.chatapp.repository.LocalMessagesRepo;
import marcelin.thierry.chatapp.utils.Constant;

public class MessageViewModel extends AndroidViewModel {
    private LocalMessagesRepo localMessagesRepo;

    public MessageViewModel(@NonNull Application application) {
        super(application);
        localMessagesRepo = new LocalMessagesRepo(application);
    }

    public LiveData<List<Message>> getAllMessagesByConversation(String conversationId) {
        return localMessagesRepo.getAllById(conversationId);
    }

    public LiveData<List<Message>> getUnseenMessageForConversation(String conversationId) {
        return localMessagesRepo.getAllForParam(Constant.UNSEEN_MSG, conversationId);
    }

    public LiveData<Message> getLastMessageForConversation(String conversationId) {
        return localMessagesRepo.getOneByParam(Constant.LAST_MSG, conversationId);
    }

    public void insertMessage(Message message) {
        localMessagesRepo.save(message);
    }

    public void updateMessage(Message message) {
        localMessagesRepo.update(message);
    }

}
