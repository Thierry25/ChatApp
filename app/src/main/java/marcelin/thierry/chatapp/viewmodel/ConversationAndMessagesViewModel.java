package marcelin.thierry.chatapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import marcelin.thierry.chatapp.dao.ConversationAndMessageDao;
import marcelin.thierry.chatapp.dto.ConversationAndMessage;
import marcelin.thierry.chatapp.persistance.ConversationsDatabase;

public class ConversationAndMessagesViewModel extends AndroidViewModel {
    private ConversationAndMessageDao conversationAndMessageDao;

    public ConversationAndMessagesViewModel(@NonNull Application application) {
        super(application);

        ConversationsDatabase conversationsDb = ConversationsDatabase.getInstance(application);
        conversationAndMessageDao = conversationsDb.conversationAndMessageDao();
    }

    public LiveData<ConversationAndMessage> getMessagesForConversation(String conversationId) {
        return conversationAndMessageDao.getOneById(conversationId);
    }
}
