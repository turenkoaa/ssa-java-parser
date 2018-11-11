package com.kspt.aaturenko;

/**
 * Created by Anastasia on 11.11.2018.
 */
public class IdGenerator {
    private static volatile long nextId = 0;

    public static long getNextId(){
        return ++nextId;
    }
}
