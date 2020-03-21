package ru.itmo.java;


public class HashTable {
    private final static int RESIZE_FACTOR = 2;
    private final static int NEXT_ITEM_STEP = 1;
    private final static float DEFAULT_LOAD_FACTOR = 0.5f;
    private final static int DEFAULT_CAPACITY = 1 << 4;
    private final static int MAXIMUM_CAPACITY = 1 << 30;

    private Entry[] table;
    private boolean[] deletedElements;
    private int currentSize = 0;
    private int capacity;
    private float loadFactor;
    private int threshold;

    public HashTable() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public HashTable(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    public HashTable(int capacity, float loadFactor) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Invalid table capacity: " + capacity);
        }
        if (loadFactor <= 0 || loadFactor - 1e-6f > 1.0f) {
            throw new IllegalArgumentException("Invalid load factor: " + loadFactor);
        }
        this.capacity = capacity;
        this.loadFactor = loadFactor;
        this.threshold = (int) (capacity * loadFactor);
        this.table = new Entry[capacity];
        this.deletedElements = new boolean[capacity];
    }

    public Object put(Object key, Object value) {
        if (currentSize == MAXIMUM_CAPACITY) {
            throw new RuntimeException("Reached maximum capacity: " + currentSize);
        }
        Entry newElement = new Entry(key, value);
        int foundIndex = getIndexByKey(key);
        if (table[foundIndex] == null) {
            foundIndex = getHash(key);
            while (table[foundIndex] != null) {
                foundIndex = (foundIndex + NEXT_ITEM_STEP) % capacity;
            }
            table[foundIndex] = newElement;
            deletedElements[foundIndex] = false;
            if (++currentSize >= threshold) {
                resize();
            }
            return null;
        }
        Object oldValue = table[foundIndex].getValue();
        table[foundIndex] = newElement;
        return oldValue;
    }

    public Object get(Object key) {
        Entry element = table[getIndexByKey(key)];
        return element == null ? null : element.getValue();
    }

    public Object remove(Object key) {
        int foundIndex = getIndexByKey(key);
        if (table[foundIndex] == null) {
            return null;
        }
        Object oldValue = table[foundIndex].getValue();
        deletedElements[foundIndex] = true;
        table[foundIndex] = null;
        --currentSize;
        return oldValue;
    }

    public int size() {
        return currentSize;
    }

    private void resize() {
        capacity *= RESIZE_FACTOR;
        threshold = (int) (capacity * loadFactor);
        deletedElements = new boolean[capacity];
        Entry[] oldElements = table;
        table = new Entry[capacity];
        currentSize = 0;
        for (Entry currentElement : oldElements) {
            if (currentElement != null) {
                put(currentElement.getKey(), currentElement.value);
            }
        }

    }

    private int getHash(Object key) {
        return Math.abs(key.hashCode()) % capacity;
    }

    private int getIndexByKey(Object key) {
        int hashKey = getHash(key);
        while (deletedElements[hashKey] || ( table[hashKey] != null && ! table[hashKey].getKey().equals(key))) {
            hashKey = (hashKey + NEXT_ITEM_STEP) % capacity;
        }
        return hashKey;
    }

    private final static class Entry {
        private final Object key;
        private final Object value;

        public Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }
}