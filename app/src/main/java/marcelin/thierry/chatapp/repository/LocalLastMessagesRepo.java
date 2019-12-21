package marcelin.thierry.chatapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dao.ConversationAndLastMessageDao;
import marcelin.thierry.chatapp.dto.ConversationAndLastMessage;
import marcelin.thierry.chatapp.persistance.ConversationsDatabase;

public class LocalLastMessagesRepo implements IDatabaseProvider<ConversationAndLastMessage> {
    private ConversationsDatabase db;
    private ConversationAndLastMessageDao conversationAndLastMessageDao;
    private LiveData<List<ConversationAndLastMessage>> lastMessageList;
    private LiveData<List<ConversationAndLastMessage>> lastMessageForConversation;

    public LocalLastMessagesRepo(Application application) {
        db = ConversationsDatabase.getInstance(application);
        conversationAndLastMessageDao = db.conversationAndLastMessageDao();
        lastMessageList = conversationAndLastMessageDao.getAll();
    }

    public LocalLastMessagesRepo(Application application, String conversationId) {
        db = ConversationsDatabase.getInstance(application);
        conversationAndLastMessageDao = db.conversationAndLastMessageDao();
        lastMessageForConversation = conversationAndLastMessageDao.getAllById(conversationId);
    }

    @Override
    public LiveData<List<ConversationAndLastMessage>> getAll() {
        return lastMessageList;
    }

    @Override
    public LiveData<List<ConversationAndLastMessage>> getAllById(String id) {
        return conversationAndLastMessageDao.getAllById(id);
    }

    @Override
    public LiveData<List<ConversationAndLastMessage>> getAllForParam(String param, String id) {
        return null;
    }

    @Override
    public LiveData<ConversationAndLastMessage> getOne(String id) {
        return null;
    }

    @Override
    public LiveData<ConversationAndLastMessage> getOne() {
        return null;
    }

    @Override
    public LiveData<ConversationAndLastMessage> getOneByParam(String param, String id) {
        return null;
    }

    @Override
    public void save(ConversationAndLastMessage conversationAndLastMessageDao) {
        //Nothing to do
    }

    @Override
    public void update(ConversationAndLastMessage conversationAndLastMessageDao) {
        //Nothing to do
    }

    @Override
    public void delete(ConversationAndLastMessage conversationAndLastMessageDao) {
        //Nothing to do
    }
}
