package com.yy.skiplist.stresstest;

import com.yy.skiplist.SkipList;

import java.util.Random;
import java.util.concurrent.*;

public class StressTest {
    // test the SkipList using 100,000 threads
    // record the time consuming of insertion and query

    public static void main(String[] args) throws InterruptedException {
        int total = 1000000;         // The total number of records have to be store in the SkipList
        int corePoolSize = 5;        // Some necessary parameters have to set for the thread pool
        int maximumPoolSize = 10;
        long keepAliveTime = 10000L;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        ThreadPoolExecutor executor =   // The thread pool to insert or query records
                new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new LinkedBlockingDeque<>());
        CountDownLatch latch = new CountDownLatch(total);  // make the main thread wait for complete of all sub threads
        SkipList<Integer, String> skipList = new SkipList<>();

        // insert the data
        Integer[] keys1 = randomArray(0, total - 1, total);
        long start = System.currentTimeMillis();
        for (int i = 0; i < total; i++) {
            assert keys1 != null;
            executor.execute(new MyThreadToInsert<>(latch, skipList, keys1[i], Integer.toString(keys1[i])));
        }
        executor.shutdown();
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("The number of inserted records: " + skipList.length());
        System.out.println("The depth of the SkipList: " + skipList.level());
        System.out.println("Time consuming: " + (end - start) + "ms");
        System.out.println();

//        skipList.save("d:/");  // save the data
        // query
        ThreadPoolExecutor executor2 =   // The thread pool to insert or query records
                new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                        new LinkedBlockingDeque<>());
        latch = new CountDownLatch(total);

        Integer[] keys2 = randomArray(0, total - 1, total);
        start = System.currentTimeMillis();
        for (int i = 0; i < total; i++) {
            assert keys2 != null;
            executor2.execute(new MyThreadToQuery<>(latch, skipList, keys2[i]));
        }
        executor2.shutdown();
        while (true) {
            if (executor2.getQueue().size() == 0 && executor2.getActiveCount() == 0) break;
        }
        end = System.currentTimeMillis();
        System.out.println("The number of records to query: " + total);
        System.out.println("Time consuming: " + (end - start) + "ms");
    }

    // get the array containing random query keys
    private static Integer[] randomArray(int min, int max, int n){
        int len = max-min+1;

        if(max < min || n > len){
            return null;
        }

        // initialize the array of candidates for a given range
        int[] source = new int[len];
        for (int i = min; i < min+len; i++){
            source[i - min] = i;
        }

        Integer[] result = new Integer[n];
        Random rd = new Random();
        int index = 0;
        for (int i = 0; i < result.length; i++) {
            // A random index of the array to be selected from 0 to (len-2)
            index = Math.abs(rd.nextInt() % len--);
            // put the random number into the result set
            result[i] = source[index];
            // replace the random number in the array to be selected with the number corresponding to the subscript of the array to be selected (len-1)
            source[index] = source[len];
        }
        return result;
    }

    // create a thread to insert an element
    private static class MyThreadToInsert<K extends Comparable<K>,V> implements Runnable {

        private CountDownLatch latch;
        private SkipList<K,V> skipList;
        private K key;
        private V value;

        public MyThreadToInsert(CountDownLatch latch, SkipList<K,V> skipList, K key, V value) {
            this.latch = latch;
            this.skipList = skipList;
            this.key = key;
            this.value = value;
        }

        @Override
        public void run() {
            int insert = skipList.insert(key, value);
            if(insert == 0) System.out.println("fail to insert");
            latch.countDown();
        }
    }

    // create a thread to query an element
    private static class MyThreadToQuery<K extends Comparable<K>,V> implements Runnable {

        private CountDownLatch latch;
        private SkipList<K,V> skipList;
        private K key;

        public MyThreadToQuery(CountDownLatch latch, SkipList<K,V> skipList, K key) {
            this.latch = latch;
            this.skipList = skipList;
            this.key = key;
        }

        @Override
        public void run() {
            V c = skipList.query(key).getValue();
            if(c == null) System.out.println("fail to query");
            latch.countDown();
        }
    }
}
