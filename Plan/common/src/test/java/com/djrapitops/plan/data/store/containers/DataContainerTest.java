package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.data.store.Key;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for {@link DataContainer} programming errors.
 *
 * @author Rsl1122
 */
public class DataContainerTest {

    private static final Key<String> TEST_KEY = new Key<>(String.class, "TEST_KEY");
    private static final Key<String> TEST_KEY_COPY = new Key<>(String.class, "TEST_KEY");

    @Test
    public void safeUnsafeKeySupplierSameObject() {
        DataContainer container = new DataContainer();
        container.putSupplier(TEST_KEY, () -> "Success");

        assertEquals("Success", container.getUnsafe(TEST_KEY));
    }

    @Test
    public void safeUnsafeKeySupplierDifferentObject() {
        DataContainer container = new DataContainer();
        container.putSupplier(TEST_KEY, () -> "Success");

        assertEquals("Success", container.getUnsafe(TEST_KEY_COPY));
    }

    @Test
    public void safeUnsafeKeyRawSameObject() {
        DataContainer container = new DataContainer();
        container.putRawData(TEST_KEY, "Success");

        assertEquals("Success", container.getUnsafe(TEST_KEY));
    }

    @Test
    public void safeUnsafeKeyRawDifferentObject() {
        DataContainer container = new DataContainer();
        container.putRawData(TEST_KEY, "Success");

        assertEquals("Success", container.getUnsafe(TEST_KEY_COPY));
    }

    @Test
    public void safeUnsafeKeyRawNull() {
        DataContainer container = new DataContainer();
        container.putRawData(TEST_KEY, null);

        assertTrue(container.supports(TEST_KEY));
        assertNull(container.getUnsafe(TEST_KEY));
    }

    @Test
    public void safeUnsafeKeyNullSupplier() {
        DataContainer container = new DataContainer();
        container.putSupplier(TEST_KEY, null);

        assertFalse(container.supports(TEST_KEY));
    }

    @Test
    public void safeUnsafeKeySupplierNull() {
        DataContainer container = new DataContainer();
        container.putSupplier(TEST_KEY, () -> null);

        assertTrue(container.supports(TEST_KEY));
        assertNull(container.getUnsafe(TEST_KEY));
    }

    @Test
    public void cachingSupplier() {
        DataContainer container = new DataContainer();
        String firstObj = "First";
        String secondObj = "Second";

        assertNotSame(firstObj, secondObj);

        container.putCachingSupplier(TEST_KEY, () -> firstObj);

        String found = container.getUnsafe(TEST_KEY);
        assertEquals(firstObj, found);
        assertSame(firstObj, found);
        assertNotSame(secondObj, found);

        String secondCall = container.getUnsafe(TEST_KEY);
        assertSame(found, secondCall);
    }

}