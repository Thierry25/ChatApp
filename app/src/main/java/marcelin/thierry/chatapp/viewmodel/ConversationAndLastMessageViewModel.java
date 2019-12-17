package marcelin.thierry.chatapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dao.ConversationAndLastMessageDao;
import marcelin.thierry.chatapp.dto.ConversationAndLastMessage;
import marcelin.thierry.chatapp.persistance.ConversationsDatabase;

public class ConversationAndLastMessageViewModel extends AndroidViewModel {
    private ConversationAndLastMessageDao cLastMessageDao;

    public ConversationAndLastMessageViewModel(@NonNull Application application) {
        super(application);
        ConversationsDatabase conversationsDb = ConversationsDatabase.getInstance(application);
        cLastMessageDao = conversationsDb.conversationAndLastMessageDao();
    }

    public LiveData<List<ConversationAndLastMessage>> getAllConversation() {
        return cLastMessageDao.get();
    }

    public LiveData<ConversationAndLastMessage> getConversationById(String conversationId) {
        return cLastMessageDao.getOneById(conversationId);
    }

}
