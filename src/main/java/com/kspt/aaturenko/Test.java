package com.kspt.aaturenko;

public class Test {
    public void doMethod(){
        int a = 1;
        int b = 2;
        int c = 0;
        if (a > b) {
            c = 3;
            b = 3;
        } else {
            c = 4;
        }
        b=c;
        a=6;

    }
}
