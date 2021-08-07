package com.yy.skiplist;

import java.io.*;
import java.util.HashMap;
import java.util.Random;

public class SkipList<K extends Comparable<K>,V> {
    // the data structure of zset implemented in redis
    private SkipListNode<K,V> head;     // pointer to head
    private int level;                 // the highest level
    private int length;                // the number of members

    public SkipList() {
        this.head = new SkipListNode<>();
        this.level = 1;
    }

    public SkipList(int num) {
        this.head = new SkipListNode<>(num);
        this.level = 1;
    }

    public int length() {
        return length;
    }

    public int level() {
        return level;
    }

    /**
     * insert a node with KV into the SkipList
     * @param key key
     * @param value value
     * @return 1 for success, 0 for failure
     */
    public synchronized int insert(K key, V value) {

        // find the suitable position to insert
        SkipListNode<K,V> cur = head;
        // create update array to record each level of the new node to insert
        SkipListNode<K,V>[] update = new SkipListNode[level + 1];
        for(int i = update.length - 1;i >= 0;i--) {
            // find the suitable position in each level from top to bottom
            while (cur.forwards[i] != null && cur.forwards[i].getKey().compareTo(key) < 0) {
                cur = cur.forwards[i];
            }
            // record the position of the backward node
            update[i] = cur;
        }

        // generate a level value
        int level = Math.min(getRandomLevel(), head.forwards.length);
        // update if the level is higher than the highest one
        if (level > this.level) this.level = level;

        // create node to insert into the SkipList
        SkipListNode<K,V> node = createNode(key, value, level);
        // insert the node in each level
        for (int i = 0; i < level; i++) {
            node.forwards[i] = update[i].forwards[i];
            update[i].forwards[i] = node;
        }

        // update the new node backward
        node.backward = update[0] == head ? null : update[0];
        if (node.forwards[0] != null) node.forwards[0].backward = node;
        // update the length of members
        this.length++;

        return 1;
    }


    /**
     * delete the node with KV from the SkipList
     * @param key key
     * @return 1 for success, 0 for failure
     */
    public synchronized SkipListNode<K,V> delete(K key) {
        // find the suitable position to insert
        SkipListNode<K,V> cur = head;

        // create update array to record each level of the new node to insert
        SkipListNode<K,V>[] update = new SkipListNode[level + 1];
        for(int i = update.length - 1;i >= 0;i--) {
            // find the suitable position in each level from top to bottom
            while (cur.forwards[i] != null && cur.forwards[i].getKey().compareTo(key) < 0) {
                cur = cur.forwards[i];
            }
            // record the position of the backward node
            update[i] = cur;
        }

        cur = update[0].forwards[0];
        // change the backward node of cur
        if (cur.forwards[0] != null)
            cur.forwards[0].backward = cur.backward;
        // delete the node in each level
        for (int i = 0; i < level; i++) {
            if (update[i].forwards[i] == cur) {
                update[i].forwards[i] = cur.forwards[i];
            } else break;
        }
        // remove levels which have no element
        while(level > 0 && head.forwards[level - 1] == null) --level;
        --length;

        return cur;
    }

    /**
     * query the node if exists
     * @param key key
     * @return true for existing, false for non-existing
     */
    public boolean contains(K key) {
        return query(key) != null;
    }

    /**
     * query the node
     * @param key key
     * @return the node
     */
    public SkipListNode<K,V> query(K key) {
        SkipListNode<K,V> cur = head;
        // query from the highest level
        for(int i = level - 1;i >= 0;i--) {
            // find the suitable position in each level from top to bottom
            while (cur.forwards[i] != null && cur.forwards[i].getKey().compareTo(key) < 0) {
                cur = cur.forwards[i];
            }
            if (cur.forwards[i] != null && cur.forwards[i].getKey().compareTo(key) == 0) return cur.forwards[i];
        }
        return null;
    }

    /**
     * save the data
     * @param path path to save
     */
    public void save(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            System.out.println("Invalid path !!");
            System.out.println("Fail to save ...");
            return;
        }
        File file = new File(dir + "/data");
        SkipListNode<K,V> cur = head.forwards[0];
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            while(cur != null) {
                oos.writeObject(cur.getKey());
                oos.writeObject(cur.getValue());
                cur = cur.forwards[0];
            }
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Save successfully !!");
        System.out.println("The file size: " + getFileSize(file));
    }

    /**
     * load the data
     * @param path path to load
     */
    public void load(String path) {
        File file = new File(path);
        if (!file.exists() || !file.getName().endsWith("data")) {
            System.out.println("Invalid file !!");
            System.out.println("Fail to load ...");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            K key;
            while ((key = (K) ois.readObject()) != null) {
                insert(key, (V) ois.readObject());
            }
        } catch (EOFException e) {

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Load successfully !!");
        System.out.println("The file size: " + getFileSize(file));
    }

    /**
     * show the whole data structure
     */
    public void show() {
        System.out.println("======================= SkipList =======================");
        System.out.println();
        HashMap<SkipListNode<K,V>,Integer> map = new HashMap<>();
        SkipListNode<K,V> cur = head;
        int index = 0;
        while (cur.forwards[0] != null) {
            map.put(cur.forwards[0], index++);
            cur = cur.forwards[0];
        }
        for (int i = level - 1;i >= 0;i--) {
            System.out.print("Level " + i + "\t");
            SkipListNode<K,V> node = head.forwards[i];
            for (int j = 0; j < length; j++) {
                if (node != null && map.get(node) == j) {
                    System.out.print("\t" + node.getKey());
                    node = node.forwards[i];
                } else System.out.print("\t");
            }
            System.out.println();
        }
        System.out.println("========================================================");
    }

    /**
     * get the random height (lower than maximum level) for the node
     * @return the random height
     */
    private int getRandomLevel() {
        Random random = new Random();
        int height = 1;
        while(height <= level && random.nextInt(2) > 0) {
            height++;
        }
        return height;
    }

    /**
     * create a node in the SkipList
     * @param key key
     * @param value value
     * @param level the height of the node
     * @return a created node
     */
    private SkipListNode<K,V> createNode(K key, V value, int level) {
        return new SkipListNode<>(key, value, level);
    }

    /**
     * get the file size
     * @param file file
     * @return the file size
     */
    private String getFileSize(File file) {
        if (!file.exists()) return "invalid file";
        long length = file.length();
        if (length < 1024L) return length + " B";
        if (length < 1048576L) return ((double)length / 1024) + " KB";
        if (length < 1073741824L) return ((double)length / 1048576L) + " MB";
        if (length < 1099511627776L) return ((double)length / 1073741824L) + " GB";
        return "unknown size";
    }
}
