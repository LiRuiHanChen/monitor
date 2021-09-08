package com.monitor.argent.commons;


public class Calculator {

    public int addFuction(int a, int b) {
        return a / b;
    }

    public int evaluate(String expression) {
        int sum = 0;
        for (String summand : expression.split("\\+"))
            sum += Integer.valueOf(summand);
        System.out.println("Hello World!!");
        return sum;
    }

}
