package pool;

import java.util.function.Supplier;

public interface ExpirationObjectsPool<T> extends SimpleObjectPool<T> {

    long getExpirationTime();

    boolean isValid(T obj);

    static <T> Builder<T> define() {
        return new pool.GenericExpirationObjectsPool.Builder<T>();
    }

    interface Builder<T> {

        Builder<T> expirationTime(long time);

        Builder<T> minSize(int size);

        Builder<T> waitTime(long time);

        Builder<T> maxSize(int size);

        Builder<T> factory(Supplier<T> supplier);


        ExpirationObjectsPool<T> build();

    }

}
