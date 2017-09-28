import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pool.ExpirationObjectsPool;
import task.User;

import java.util.NoSuchElementException;
import java.util.Random;

public class ObjectPoolTest {

    private ExpirationObjectsPool<User> genericObjectPool;
    private long expirationTime;
    private int objectPoolSize;

    @Before
    public void objectPoolInit() {
        Random r = new Random();
        expirationTime = r.nextInt(2000) + 1000;
        objectPoolSize = 10;
        genericObjectPool = ExpirationObjectsPool.<User>define().factory(User::new)
                .expirationTime(expirationTime)
                .maxSize(objectPoolSize)
                .build();

    }

    @Test(expected = NullPointerException.class)
    public void testNullFactoryMethod() {
        genericObjectPool = ExpirationObjectsPool.<User>define().factory(null).waitTime(expirationTime)
                .maxSize(objectPoolSize).build();
    }

    @Test
    public void testGetOneObjectFromObjectPool() {
        User user = genericObjectPool.get();
        Assert.assertNotNull("new user is expected", user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePoolSize() {
        ExpirationObjectsPool.<User>define().factory(User::new).waitTime(expirationTime).maxSize(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeExpirationTime() {
        ExpirationObjectsPool.<User>define().factory(User::new).waitTime(-1).maxSize(objectPoolSize).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeExpirationTimeAndPoolSize() {
        ExpirationObjectsPool.<User>define().factory(User::new).waitTime(-1).maxSize(-1).build();
    }

    @Test
    public void testPoolTotalObjectsInPool() {

        Assert.assertEquals("Empty pool expected", 0, genericObjectPool.instantiatedObjectsAmount());

        User user = genericObjectPool.get();
        User user2 = genericObjectPool.get();

        Assert.assertEquals("Expected Pool filled with 2 objects (in busy stack)", 2,
                genericObjectPool.instantiatedObjectsAmount());

        genericObjectPool.returnBack(user);
        genericObjectPool.returnBack(user2);

        Assert.assertEquals("Expected Pool filled with 2 objects (merged from busy to free stack)", 2,
                genericObjectPool.instantiatedObjectsAmount());

    }


    public void testOverfillObjectPool() {
        for (int i = 0; i < objectPoolSize; i++) {
            User user = genericObjectPool.get();
            user.setAddress("Main Str.");
            user.setName("John" + i);
            Assert.assertNotNull("New user is expected", user);
        }

        User u = genericObjectPool.get();
        System.out.println(u);
    }

    @Test
    public void testIfAssignedExpirationTime() {
        Assert.assertEquals("Test if expirationTime  was assigned ", genericObjectPool.getExpirationTime(),
                expirationTime);
    }

    @Test(expected = NoSuchElementException.class)
    public void testClosingUnrecognizedObject() {
        User user = new User();
        user.setName("John Travolta");
        user.setAddress("Travolta Str.");
        genericObjectPool.returnBack(user);

    }

    @Test
    public void testCloseObject() {
        User user = genericObjectPool.get();

        genericObjectPool.returnBack(user);

        User newUser = genericObjectPool.get();

        Assert.assertEquals("The same object after closing returning/requesting expected", user, newUser);
    }

    @Test
    public void testIfObjectIsValid() throws InterruptedException {
        User user = genericObjectPool.get();

        Assert.assertTrue("Object 'lifeTime' is not expired.", genericObjectPool.isValid(user));

        Thread.sleep(expirationTime);

        Assert.assertFalse("Object 'lifeTime' is expired", genericObjectPool.isValid(user));
    }

    @Test
    public void testValidateUnknownObject() {
        User user = new User();
        user.setName("Will Smith");
        user.setAddress("Smith & Co Str.");
        Assert.assertFalse("Uknown object false expected", genericObjectPool.isValid(user));

    }

    @Test
    public void testFreeObjectsSize() {
        User u = genericObjectPool.get();
        genericObjectPool.returnBack(u);
        Assert.assertEquals("1 object in freeObjectStack is expected", 1, genericObjectPool.freeObjectsAmount());

    }

    @Test
    public void testBusyObjectsSize() {
        genericObjectPool.get();
        Assert.assertEquals("1 object in busyObjectStack is expected", 1, genericObjectPool.busyObjectsAmount());
    }

    @Test
    public void testExpiredObjectRemoving() throws InterruptedException {

        testGetOneObjectFromObjectPool();
        Thread.currentThread().sleep(expirationTime + 1000);
        Assert.assertEquals("expected expired object to be removed", 0, genericObjectPool.busyObjectsAmount());

    }

}
