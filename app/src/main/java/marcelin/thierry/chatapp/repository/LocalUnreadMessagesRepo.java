package marcelin.thierry.chatapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dao.ConversationAndUnreadMessageDao;
import marcelin.thierry.chatapp.dto.ConversationAndUnreadMessage;
import marcelin.thierry.chatapp.persistance.ConversationsDatabase;

public class LocalUnreadMessagesRepo implements IDatabaseProvider<ConversationAndUnreadMessage> {

    private ConversationAndUnreadMessageDao conversationAndMessageDao;
    private LiveData<List<ConversationAndUnreadMessage>> conversationList;
    private LiveData<ConversationAndUnreadMessage> conversation;

    public LocalUnreadMessagesRepo(Application application) {
        ConversationsDatabase db = ConversationsDatabase.getInstance(application);
        conversationAndMessageDao = db.conversationAndUnreadMessageDao();
        conversationList = conversationAndMessageDao.getAllUnreadMessages();
    }

    public LocalUnreadMessagesRepo(Application application, String conversationId) {
        ConversationsDatabase db = ConversationsDatabase.getInstance(application);
        conversationAndMessageDao = db.conversationAndUnreadMessageDao();
        conversation = conversationAndMessageDao.getUnreadMessagesForOne(conversationId);
    }

    @Override
    public LiveData<List<ConversationAndUnreadMessage>> getAll() {
        return conversationList;
    }

    @Override
    public LiveData<List<ConversationAndUnreadMessage>> getAllById(String id) {
        return null;
    }

    @Override
    public LiveData<List<ConversationAndUnreadMessage>> getAllForParam(String param, String id) {
        return null;
    }

    @Override
    public LiveData<ConversationAndUnreadMessage> getOne(String id) {
        return conversationAndMessageDao.getUnreadMessagesForOne(id);
    }

    @Override
    public LiveData<ConversationAndUnreadMessage> getOne() {
        return conversation;
    }

    @Override
    public LiveData<ConversationAndUnreadMessage> getOneByParam(String param, String id) {
        return null;
    }

    @Override
    public void save(ConversationAndUnreadMessage conversationAndMessage) {
        //nothing to do
    }

    @Override
    public void update(ConversationAndUnreadMessage conversationAndMessage) {
        //nothing to do
    }

    @Override
    public void delete(ConversationAndUnreadMessage conversationAndMessage) {
        //nothing to do
    }
}
