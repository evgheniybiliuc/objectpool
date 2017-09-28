package pool;

import util.Numbers;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

final class GenericExpirationObjectsPool<T> implements ExpirationObjectsPool<T> {

    private static final long TIMER_DELAY_TIME = 0;

    private static final long PERIOD_TIME = 1000;
    private final long expirationTime;

    private BlockingQueue<T> freeObjects;

    private Map<T, Long> busyObjects;

    private final int poolSize;

    private final Supplier<T> factory;


    private GenericExpirationObjectsPool(Builder<T> builder) {
        factory = builder.factory;
        expirationTime = builder.expirationTime;
        poolSize = builder.poolSize;

        freeObjects = new LinkedBlockingQueue<>(poolSize);
        busyObjects = new ConcurrentHashMap<>(poolSize);

        generateObjects(builder.minSize);

        runTimer();
    }

    //ToDO: there are a moments where timer iterating trough objects identify them as valid moving to next one And object is becoming expired.
    //ToDo: to clarify with mentor.
    private void runTimer() {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {

                busyObjects.forEach((key, value) -> {
                    if (!isValid(key))
                        busyObjects.remove(key);
                });

            }
        }, TIMER_DELAY_TIME, PERIOD_TIME);
    }

    private void generateObjects(int minSize) {

        try {
            for (int i = 0; i < minSize; i++)
                freeObjects.put(factory.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public T get() {
        ensureCapacity();
        T obj = null;
        try {
            obj = freeObjects.take();
            busyObjects.put(obj, System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return obj;
    }

    private void ensureCapacity() {
        int totalObjects = freeObjectsAmount() + busyObjectsAmount();
        try {
            if (poolSize - totalObjects > 0 && freeObjects.isEmpty())
                freeObjects.put(factory.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void returnBack(T obj) {
        if (busyObjects.remove(obj) == null)
            throw new NoSuchElementException("Unknown object");
        freeObjects.add(obj);
    }

    @Override
    public int instantiatedObjectsAmount() {
        return busyObjects.size() + freeObjects.size();
    }

    @Override
    public long getExpirationTime() {
        return expirationTime;
    }

    @Override
    public boolean isValid(T obj) {

        Long expireTime = busyObjects.get(obj);

        if (expireTime == null)
            return false;

        long workTime = System.currentTimeMillis() - expireTime;

        return workTime < expirationTime;

    }

    @Override
    public int freeObjectsAmount() {
        return freeObjects.size();
    }

    @Override
    public int busyObjectsAmount() {
        return busyObjects.size();
    }

    public static final class Builder<T> implements pool.ExpirationObjectsPool.Builder<T> {
        private int poolSize;
        private long expirationTime;
        private int minSize;
        private Supplier<T> factory;
        private long waitTime;


        public Builder<T> expirationTime(long time) {
            expirationTime = time;
            return this;
        }

        public Builder<T> minSize(int size) {
            minSize = size;
            return this;
        }

        public Builder<T> waitTime(long time) {
            waitTime = time;
            return this;
        }

        public Builder<T> maxSize(int size) {
            poolSize = size;
            return this;
        }

        public Builder<T> factory(Supplier<T> supplier) {
            factory = supplier;
            return this;
        }


        public ExpirationObjectsPool<T> build() {
            Objects.requireNonNull(factory);
            Numbers.require(poolSize, (n) -> n > 0);
            Numbers.require(expirationTime, (n) -> n > 0);
            return new GenericExpirationObjectsPool<T>(this);
        }

        //
        // static void main(String[] args) {
        // ExpirationObjectsPool<Connection> p =
        // ExpirationObjectsPool.<Connection>define().
        // withExpirationTime().withObjectFactory(()->{
        // try {
        // return DriverManager.getConnection("jk:localhost:8056/");
        // }catch(Exception e) {
        // throw new IllegalStateException("Can't obtain connection...", e);
        // }
        // }).build();
        // }
    }


}
