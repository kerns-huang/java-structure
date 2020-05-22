package com.kerns.structure.tree;

import org.junit.jupiter.api.Test;

class BPlugsTree2Test {

    @Test
    public void testInsert(){
        BPlugsTree2<Integer,String> tree=new BPlugsTree2<>(4);
        for(int i=1;i<=100;i++){
            tree.insert(i,"test "+i);
        }
    }


    public void testSearch(){

    }


    public void testDelete(){
        BPlugsTree2<Integer,String> tree=new BPlugsTree2<>(4);
        for(int i=1;i<=10;i++){
            tree.insert(i,"test "+i);
        }


    }




}