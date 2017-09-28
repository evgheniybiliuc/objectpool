package pool;


public interface SimpleObjectPool<T> {

    T get();

    void returnBack(T obj);

    int instantiatedObjectsAmount();
    
    int freeObjectsAmount();
    
    int busyObjectsAmount();
}
