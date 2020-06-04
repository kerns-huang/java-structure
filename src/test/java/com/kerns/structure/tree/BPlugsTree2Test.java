package com.kerns.structure.tree;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BPlugsTree2Test {

    @Test
    public void testInsert() {
        BPlugsTree2<Integer, String> tree = new BPlugsTree2<>(4);
        for (int i = 1; i <= 20; i++) {
            tree.insert(i, "test " + i);
        }
        Assertions.assertEquals(100, tree.size());
    }

    @Test
    public void testDelete() {

        BPlugsTree2<Integer, String> tree = new BPlugsTree2<>(4);
        for (int i = 1; i <= 20; i++) {
            tree.insert(i, "test " + i);
        }

        for (int i = 10; i <= 20; i++) {
            tree.delete(i);
        }

    }


    public static void main(String[] args) {
        Random random = new Random();
        System.out.println(~2);// -(x+1)
    }

}