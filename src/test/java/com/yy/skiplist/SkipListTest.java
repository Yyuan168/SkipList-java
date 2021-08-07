package com.yy.skiplist;

import org.junit.Test;

import java.util.Random;
import java.util.Scanner;


public class SkipListTest {

    @Test
    public void test01() {
        int n = 100000;
        SkipList<Integer, Character> integerCharacterSkipList = new SkipList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            integerCharacterSkipList.insert(i, 'a');
        }
        long end = System.currentTimeMillis();
        System.out.println("Time consuming: " + (end - start) + "ms");

    }

    @Test
    public void test() {
        int n = 26;
        int[] nums = randomArray(0, n - 1, n);
        SkipList<Integer,Character> skipList = new SkipList<>();
        for (int num : nums) {
            skipList.insert(num, (char) ('a' + num));
        }
        skipList.show();
        System.out.println("find e: " + skipList.query('e' - 'a').getValue());
        System.out.println("find j: " + skipList.query('j' - 'a').getValue());
        System.out.println("find o: " + skipList.query('o' - 'a').getValue());
        System.out.println("find x: " + skipList.query('x' - 'a').getValue());

        Scanner scanner = new Scanner(System.in);
        int key = scanner.nextInt();
        skipList.delete(key);
        skipList.show();
    }

    private static int[] randomArray(int min, int max, int n){
        int len = max-min+1;

        if(max < min || n > len){
            return null;
        }

        //初始化给定范围的待选数组
        int[] source = new int[len];
        for (int i = min; i < min+len; i++){
            source[i-min] = i;
        }

        int[] result = new int[n];
        Random rd = new Random();
        int index = 0;
        for (int i = 0; i < result.length; i++) {
            //待选数组0到(len-2)随机一个下标
            index = Math.abs(rd.nextInt() % len--);
            //将随机到的数放入结果集
            result[i] = source[index];
            //将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换
            source[index] = source[len];
        }
        return result;
    }
}