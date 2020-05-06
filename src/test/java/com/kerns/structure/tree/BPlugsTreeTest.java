package com.kerns.structure.tree;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BPlugsTreeTest {
    public static void main(String[] args){
        int desc = 0;
        int[] src={-1,0,1,2};
        int i=0;
        for (; i < src.length; ) {
            //当k 小于某个排序的值的时候，说明它的位置就是i ，后面的数据需要往后
            if (desc<=src[i]) {
                break;
            }
            i++;
        }
        System.out.println(i);
    }

    @Test
    public void testSimpleDelete(){
        BPlugsTree<Integer, String> bPlugsTree = new BPlugsTree(4);
        bPlugsTree.insert(1, "test 1");
        Assertions.assertEquals(bPlugsTree.get(1),"test 1");
        bPlugsTree.delete(1);
        Assertions.assertTrue(bPlugsTree.isEmpty());
    }

    @Test
    public void testDeleteMerge(){
        BPlugsTree<Integer, String> bPlugsTree = new BPlugsTree(4);
        for(int i=1;i<=20;i++){
            bPlugsTree.insert(i, "test "+i);
        }
        bPlugsTree.delete(3);
        Assertions.assertEquals(19,bPlugsTree.size());
        Assertions.assertEquals(null,bPlugsTree.get(3));
    }

    @Test
    public void testDelAll(){
        BPlugsTree<Integer, String> bPlugsTree = new BPlugsTree(4);
        for(int i=1;i<=20;i++){
            bPlugsTree.insert(i, "test "+i);
        }
        for(int i=1;i<=20;i++){
            bPlugsTree.delete(i);
        }
        Assertions.assertTrue(bPlugsTree.isEmpty());
        Assertions.assertEquals(null,bPlugsTree.get(3));
    }

    @Test
    public void testFistInsert() {
        //建立一个4阶的b+树
        BPlugsTree<Integer, String> bPlugsTree = new BPlugsTree(4);
        bPlugsTree.insert(1, "test 1");
        Assertions.assertEquals(bPlugsTree.get(1),"test 1");

    }

    /**
     *  测试第一次分裂，因为一个块超出阶数，所以需要生成两个同深度的节点，父节点需要插入这两个节点的关系，有可能又导致分裂
     */
    @Test
    public void testFistSplit() {
        BPlugsTree<String, String> bPlugsTree = new BPlugsTree(4);
        bPlugsTree.insert("1", "test 1");
        bPlugsTree.insert("2", "test 2");
        bPlugsTree.insert("3", "test 3");
        bPlugsTree.insert("4", "test 4");
        bPlugsTree.insert("5", "test 5");
        Assertions.assertEquals(bPlugsTree.get("5"),"test 5");
    }
    @Test
    public void testDoubleSplit(){
        BPlugsTree<Integer, String> bPlugsTree = new BPlugsTree(4);
        for(int i=1;i<=20;i++){
            bPlugsTree.insert(i, "test "+i);
        }
        Assertions.assertEquals(bPlugsTree.get(20),"test 20");
        Assertions.assertEquals(20,bPlugsTree.size());
    }
}