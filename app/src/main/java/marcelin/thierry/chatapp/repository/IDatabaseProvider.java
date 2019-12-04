package marcelin.thierry.chatapp.repository;

import java.util.List;

/**
 * Database provider defining contracts
 * @param <T> The object type
 * @author famarcelin
 */
public interface IDatabaseProvider<T> {

    T get();

    T getById(String id);

    List<T> getAll();

    List<T> getAllById(String id);

    void save(T t);

    void save(T t, String id);

    void update(T t);

    void update(T t, String id);

    void delete(T t);

    void delete(T t, String id);

}
