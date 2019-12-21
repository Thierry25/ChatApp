package marcelin.thierry.chatapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dao.MessageDao;
import marcelin.thierry.chatapp.dto.Message;
import marcelin.thierry.chatapp.persistance.ConversationsDatabase;
import marcelin.thierry.chatapp.utils.Constant;

public class LocalMessagesRepo implements IDatabaseProvider<Message> {

    private MessageDao messageDao;
    private LiveData<List<Message>> messageList;
    private LiveData<List<Message>> messages;
    private ConversationsDatabase db;

    public LocalMessagesRepo(Application application) {
        db = ConversationsDatabase.getInstance(application);
        messageDao = db.messageDao();
        messageList = messageDao.get();
    }

    public LocalMessagesRepo(Application application, String conversationId) {
        ConversationsDatabase db = ConversationsDatabase.getInstance(application);
        messageDao = db.messageDao();
        messages = messageDao.getAllByConversationId(conversationId);
    }

    //TODO: [fm] remove function not defined in interface
    public LiveData<List<Message>> getAllByConversation() {
        return messages;
    }

    @Override
    public LiveData<List<Message>> getAll() {
        return messageList;
    }

    @Override
    public LiveData<List<Message>> getAllById(String id) {
        return messageDao.getAllByConversationId(id);
    }

    @Override
    public LiveData<List<Message>> getAllForParam(String param, String id) {
        return Constant.UNSEEN_MSG.equals(param) ?
                messageDao.getUnseenMessageCountForConversation(id) : null;
    }

    @Override
    public LiveData<Message> getOne(String id) {
        return null;
    }

    @Override
    public LiveData<Message> getOne() {
        return null;
    }

    @Override
    public LiveData<Message> getOneByParam(String param, String id) {
        return Constant.LAST_MSG.equals(param) ? messageDao.getLastMessageForConversation(id) : null;
    }

    @Override
    public void save(Message message) {
        ConversationsDatabase.databaseWriteExecutor.execute(() -> messageDao.save(message));
    }

    @Override
    public void update(Message message) {
        ConversationsDatabase.databaseWriteExecutor.execute(() -> messageDao.update(message));
    }

    @Override
    public void delete(Message message) {
        ConversationsDatabase.databaseWriteExecutor.execute(() -> messageDao.delete(message));
    }
}
