package com.kerns.structure.tree;

/**
 * 标准的b+树
 * 参考动图
 * https://www.cs.usfca.edu/~galles/visualization/BPlusTree.html
 */
public class BPlugsTree2<K extends Comparable, V> {
    /**
     * 插入数据
     *
     * @param k
     * @param v
     */
    public void insert(K k, V v) {

    }

    abstract static class Node<K, V> {
        /**
         * 几阶的树
         */
        protected int m;
        /**
         * 已经包含了多少个元素
         */
        protected int size;
        /**
         * 包含的关键字
         */
        protected Object[] keys;

        protected Node(int m) {
            this.m = m;
        }

        /**
         * 插入节点，有可能返回新的根节点
         *
         * @param k
         * @param v
         * @return
         */
        protected abstract Node insert(K k, V v);

        /**
         * 删除节点
         *
         * @param k
         */
        protected abstract void delete(K k);

        /**
         *
         */
        protected abstract V search(K k);

    }

    static class Leaf<K extends Comparable, V> extends Node<K, V> {
        /**
         * 保存的值数据
         */
        private Object[] values;
        /**
         * 后面一个节点
         */
        private Leaf<K, V> next;
        /**
         * 前面一个节点
         */
        private Leaf<K, V> pre;

        protected Leaf(int m) {
            super(m);
            this.values = new Object[m];
        }

        protected Leaf(int m, Object[] keys, Object[] values) {
            super(m);
            this.size = keys.length;
            this.keys = keys;
            this.values = values;
        }

        @Override
        protected Node insert(K k, V v) {
            for (int i = 0; i < size; i++) {
                if (k.compareTo(keys[i]) < 1) {
                    size++;
                    Object[] newKeys = new Object[size];
                    Object[] newValues = new Object[size];
                    newKeys[i] = k;
                    newValues[i] = v;
                    //TODO 重新赋值
                    System.arraycopy(this.keys, 0, newKeys, 0, i);
                    System.arraycopy(this.values, 0, newValues, 0, i);
                    System.arraycopy(this.keys, i, newKeys, i + 1, size - i - 1);
                    System.arraycopy(this.keys, i, newValues, i + 1, size - i - 1);
                    this.keys = newKeys;
                    this.values = newValues;
                    //TODO 判断是否需要分裂。
                    return split();
                }
            }
            return null;
        }

        private Node split() {
            if (this.size < m) {
                return null;
            } else {
                int newSize = m / 2;
                Object[] newKeys1 = new Object[newSize];
                Object[] newValue1 = new Object[newSize];
                System.arraycopy(this.keys, 0, newKeys1, 0, newSize);
                System.arraycopy(this.keys, 0, newValue1, 0, newSize);
                this.keys = newKeys1;
                this.values = newValue1;
                int size2 = m - newSize;
                Object[] newKeys2 = new Object[size2];
                Object[] newValue2 = new Object[size2];
                System.arraycopy(this.keys, newSize, newKeys2, 0, size2);
                System.arraycopy(this.values, newSize, newValue2, 0, size2);
                this.keys = newKeys2;
                this.values = newValue2;
                Leaf leaf = new Leaf(m, newKeys2, newValue2);
                this.next = leaf;
                leaf.pre = this;
                return leaf;
            }
        }

        @Override
        protected void delete(K k) {

        }

        @Override
        protected V search(K k) {
            return null;
        }
    }


}
