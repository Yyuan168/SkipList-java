package com.yy.skiplist;

public class SkipListNode<K extends Comparable<K>,V> {
    // the node structure in the SkipList
    private static final int DEFAULT_MAX_LEVEL = 32;     // the default maximum level in redis

    private K key;                     // specified to rank nodes in redis
    private V value;                   // member
    SkipListNode<K,V> backward;        // backward pointer
    SkipListNode<K,V>[] forwards;      // levels to find the next node

    public SkipListNode() {
        this(DEFAULT_MAX_LEVEL);
    }

    public SkipListNode(int num) {
        this.forwards = new SkipListNode[num];
    }

    public SkipListNode(K key, V value, int num) {
        this(num);
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
