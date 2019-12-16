package marcelin.thierry.chatapp.viewmodel;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dao.MessageDao;
import marcelin.thierry.chatapp.dto.Message;
import marcelin.thierry.chatapp.persistance.ConversationsDatabase;

public class MessageViewModel extends AndroidViewModel {
    private MessageDao messageDao;
    private final InsertAsyncTask insertAsyncTask = new InsertAsyncTask();
    private final UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask();

    public MessageViewModel(@NonNull Application application) {
        super(application);

        ConversationsDatabase messageDb = ConversationsDatabase.getInstance(application);
        messageDao = messageDb.messageDao();
    }

    public LiveData<List<Message>> getAllMessagesByConversation(String conversationId) {
        return messageDao.getAllByConversationId(conversationId);
    }

    public LiveData<Integer> getUnseenMessageForConversation(String conversationId) {
        return messageDao.getUnseenMessageCountForConversation(conversationId);
    }

    public LiveData<Message> getLastMessageForConversation(String conversationId) {
        return messageDao.getLastMessageForConversation(conversationId);
    }

    public void insertMessage(Message message) {
        insertAsyncTask.execute(message);
    }

    public void updateMessage(Message message) {
        updateAsyncTask.execute(message);
    }

    private class InsertAsyncTask extends AsyncTask<Message, Void, Void> {
        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.save(messages[0]);
            return null;
        }
    }

    private class UpdateAsyncTask extends AsyncTask<Message, Void, Void> {
        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.update(messages[0]);
            return null;
        }
    }
}
