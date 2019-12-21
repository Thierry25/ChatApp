package marcelin.thierry.chatapp.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * Database provider defining contracts
 * @param <T> The object type
 * @author famarcelin
 */
public interface IDatabaseProvider<T> {

    LiveData<List<T>> getAll();

    LiveData<List<T>> getAllById(String id);

    LiveData<List<T>> getAllForParam(String param, String id);

    LiveData<T> getOne(String id);

    LiveData<T> getOne();

    LiveData<T> getOneByParam(String param, String id);

    void save(T t);

    void update(T t);

    void delete(T t);
}
