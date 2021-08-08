package com.yy.skiplist;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int n = 26;
        int[] nums = randomArray(0, n - 1, n);
        SkipList<Integer,Character> skipList = new SkipList<>();
        for (int num : nums) {
            skipList.insert(num, (char) ('a' + num));
        }

//        String path = "C:\\Users\\wodem\\Desktop";
//        skipList.save(path);
        skipList.show();
        System.out.println("find e: " + skipList.query('e' - 'a').getValue());
        System.out.println("find j: " + skipList.query('j' - 'a').getValue());
        System.out.println("find o: " + skipList.query('o' - 'a').getValue());
        System.out.println("find x: " + skipList.query('x' - 'a').getValue());

        skipList.delete('e' - 'a');
        skipList.delete('o' - 'a');

        skipList.show();

//        SkipList<Integer, Character> tmp = new SkipList<>();
//        tmp.load(path + "/data");
//        tmp.show();
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
