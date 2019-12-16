package marcelin.thierry.chatapp.viewmodel;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dao.ConversationDao;
import marcelin.thierry.chatapp.dto.Conversation;
import marcelin.thierry.chatapp.persistance.ConversationsDatabase;

public class ConversationViewModel extends AndroidViewModel {
    private ConversationDao conversationDao;
    private final InsertAsyncTask insertAsyncTask = new InsertAsyncTask();
    private final UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask();
    private final DeleteAsynTask deleteAsynTask = new DeleteAsynTask();

    public ConversationViewModel(@NonNull Application application) {
        super(application);

        ConversationsDatabase conversationsDb = ConversationsDatabase.getInstance(application);
        conversationDao = conversationsDb.conversationDao();
    }

    public LiveData<List<Conversation>> getAllConversations() {
        return conversationDao.get();
    }

    public LiveData<Conversation> getConversationById(String conversationId) {
        return conversationDao.getOneById(conversationId);
    }

    public void insertConversation(Conversation conversation) {
        insertAsyncTask.execute(conversation);
    }

    public void deleteConversation(Conversation conversation) {
        deleteAsynTask.execute(conversation);
    }

    public void updateConversation(Conversation conversation) {
        updateAsyncTask.execute(conversation);
    }

    private class InsertAsyncTask extends AsyncTask<Conversation, Void, Void> {
        @Override
        protected Void doInBackground(Conversation... conversations) {
            conversationDao.save(conversations[0]);
            return null;
        }
    }

    private class UpdateAsyncTask extends AsyncTask<Conversation, Void, Void> {
        @Override
        protected Void doInBackground(Conversation... conversations) {
            conversationDao.update(conversations[0]);
            return null;
        }
    }

    private class DeleteAsynTask extends AsyncTask<Conversation, Void, Void> {
        @Override
        protected Void doInBackground(Conversation... conversations) {
            conversationDao.delete(conversations[0]);
            return null;
        }
    }
}
