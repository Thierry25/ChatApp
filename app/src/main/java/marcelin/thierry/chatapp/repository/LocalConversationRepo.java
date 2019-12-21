package marcelin.thierry.chatapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import marcelin.thierry.chatapp.dao.ConversationDao;
import marcelin.thierry.chatapp.dto.Conversation;
import marcelin.thierry.chatapp.persistance.ConversationsDatabase;

public class LocalConversationRepo implements IDatabaseProvider<Conversation> {

    private ConversationsDatabase db;
    private ConversationDao conversationDao;
    private LiveData<List<Conversation>> conversationList;

    public  LocalConversationRepo(Application application) {
        db = ConversationsDatabase.getInstance(application);
        conversationDao = db.conversationDao();
        conversationList = conversationDao.get();
    }

    @Override
    public LiveData<List<Conversation>> getAll() {
        return conversationList;
    }

    @Override
    public LiveData<List<Conversation>> getAllById(String id) {
        return null;
    }

    @Override
    public LiveData<List<Conversation>> getAllForParam(String param, String id) {
        return null;
    }

    @Override
    public LiveData<Conversation> getOne(String id) {
        return null;
    }

    @Override
    public LiveData<Conversation> getOne() {
        return null;
    }

    @Override
    public LiveData<Conversation> getOneByParam(String param, String id) {
        return null;
    }

    @Override
    public void save(Conversation conversation) {
        ConversationsDatabase.databaseWriteExecutor.execute(() -> conversationDao.save(conversation));
    }

    @Override
    public void update(Conversation conversation) {
        ConversationsDatabase.databaseWriteExecutor.execute(() -> conversationDao.update(conversation));
    }

    @Override
    public void delete(Conversation conversation) {
        ConversationsDatabase.databaseWriteExecutor.execute(() -> conversationDao.delete(conversation));
    }
}
