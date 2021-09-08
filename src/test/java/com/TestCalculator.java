package com.example.demo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CalculatorTest {

    @Test
    public void testAddFuction() {
        int result = new Calculator().addFuction(1, 1);
        System.out.println(result);
        assert (result == 1);
    }

    @Test
    public void evaluatesExpression(){
        Calculator calculator = new Calculator();
        int sum = calculator.evaluate("1+2+3");
        assertEquals(6, sum);
    }
}
