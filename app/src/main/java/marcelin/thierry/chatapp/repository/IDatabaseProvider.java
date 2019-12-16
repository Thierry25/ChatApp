package marcelin.thierry.chatapp.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * Database provider defining contracts
 * @param <T> The object type
 * @author famarcelin
 */
public interface IDatabaseProvider<T> {

    LiveData<List<T>> get();

    void save(T t);

    void update(T t);

    void delete(T t);
}
