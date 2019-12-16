package com.ayang818.github.generator;


import org.junit.Test;

public class IdGeneratorTest {

    @Test
    public void generateIdTest() {
        IdGenerator idGenerator = new IdGenerator(31, 12);
        int count = 10000;
        while (count > 0) {
            System.out.println(idGenerator.getNextId());
            count--;
        }
    }
}
