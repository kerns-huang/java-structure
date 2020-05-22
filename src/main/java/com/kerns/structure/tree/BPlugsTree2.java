package com.kerns.structure.tree;

/**
 * 标准的b+树
 * 参考动图
 * https://www.cs.usfca.edu/~galles/visualization/BPlusTree.html
 */
public class BPlugsTree2<K extends Comparable, V> {

    private Node<K, V> root;

    public BPlugsTree2(int m) {
        root = new Leaf<>(m);
    }


    public void printTree() {
        Node node = root;
        while (node.size > 0) {
            System.out.print(node.keys + " ");
            if (node instanceof NonLeaf) {
                for (int i = 0; i < node.size; i++) {
                    //打印
                }
            }
        }
    }

    /**
     * 插入数据
     *
     * @param k
     * @param v
     */
    public void insert(K k, V v) {
        //TODO 查找对应的Node节点，如果没有返回
        Node node = root;
        while (true) {
            if (node instanceof NonLeaf) {
                //非叶子节点
                int index = node.getIndex(k);
                node = ((NonLeaf) node).children[index];
            } else {
                // 如果是叶子节点，直接插入数据
                Node newNode = node.insert(k, v);
                if (root != newNode) {
                    //更新节点，默认情况下会有多线程进程，h2 使用compare and set 实现
                    root = newNode;
                }
                return;
            }
        }

    }

    /**
     * 查找数据
     *
     * @param k
     * @return
     */
    public V get(K k) {
        Node node = root;
        //遍历非叶子节点 获取叶子节点的数据
        while (node instanceof NonLeaf) {
            node = ((NonLeaf) node).children[node.getIndex(k)];
        }
        return (V) ((Leaf) node).values[node.getIndex(k)];
    }


    public void delete(K k) {
        Node node = root;
        //1:简单删除
        while (node instanceof NonLeaf) {
            node = ((NonLeaf) node).children[node.getIndex(k)];
        }
        //叶子节点删除数据
        node.delete(k);
        //从叶子节点开始修正tree 树

    }


    abstract static class Node<K extends Comparable, V> {
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
        protected Comparable[] keys;

        /**
         * 父亲节点
         */
        protected NonLeaf<K, V> parent;

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


        /**
         * 中间查找,存的数据越多，效率越高,b+树适合这种查找法
         *
         * @param k
         * @return
         */
        public int getIndex(K k) {
            int low = 0;
            int high = size - 1;
            // the cached index minus one, so that
            // for the first time (when cachedCompare is 0),
            // the default value is used
            int x = 0;
            if (x < 0 || x > high) {
                x = high >>> 1;
            }
            while (low <= high) {
                int compare = k.compareTo(keys[x]);
                if (compare > 0) {
                    low = x + 1;
                } else if (compare < 0) {
                    high = x - 1;
                } else {
                    return x;
                }
                x = (low + high) >>> 1;
            }
            return ~low;
        }
    }

    static class NonLeaf<K extends Comparable, V> extends Node<K, V> {
        /**
         * 子节点
         */
        private Node[] children;

        protected NonLeaf(int m) {
            super(m);
        }

        protected NonLeaf(int m, Comparable[] keys, Node[] children) {
            super(m);
            this.size = keys.length;
            this.keys = keys;
            this.children = children;
        }


        @Override
        protected Node insert(K k, V v) {

            return null;
        }

        @Override
        protected void delete(K k) {

        }

        @Override
        protected V search(K k) {
            int index = getIndex(k);
            // 递归查找，一直查找到叶子节点。
            return (V) children[index].search(k);
        }


        /**
         * 迭代更新插入数据
         *
         * @param left
         * @param right
         * @return
         */
        protected Node updateInsert(K k, Node<K, V> left, Node<K, V> right) {
            if (size == 0) {
                size++;
                this.keys = new Comparable[1];
                this.keys[0] = k;
                this.children = new Node[2];
                this.children[0] = left;
                this.children[1] = right;
                return this;
            } else {
                size++;
                for (int i = 0; i <= size; i++) {
                    if (children[i] == left) {
                        Comparable[] newKeys = new Comparable[size];
                        Node[] newChildren = new Node[size];
                        newKeys[i + 1] = right.keys[0];
                        newChildren[i + 1] = right;
                        System.arraycopy(this.keys, 0, newKeys, 0, i);
                        System.arraycopy(this.children, 0, newChildren, 0, i);
                        System.arraycopy(this.keys, i, newKeys, i + 1, size - i - 1);
                        System.arraycopy(this.children, i, newChildren, i + 1, size - i - 1);
                        return split();
                    }
                }
            }
            return this;
        }

        private Node split() {
            if (this.size >= m) {
                // 除以2操作
                Comparable[] oldKeys = this.keys;
                Node[] oldChildren = this.children;
                int newSize = size >> 1;
                Comparable[] newKeys1 = new Comparable[newSize];
                Node[] newChildren1 = new Node[newSize];
                System.arraycopy(oldKeys, 0, newKeys1, 0, newSize);
                System.arraycopy(oldChildren, 0, newChildren1, 0, newSize);
                this.keys = newKeys1;
                this.children = newChildren1;
                int size2 = m - newSize - 1;
                Comparable[] newKeys2 = new Comparable[size2];
                Node[] newChildren2 = new Node[size2];
                System.arraycopy(oldKeys, newSize + 1, newKeys2, 0, size2);
                System.arraycopy(oldChildren, newSize + 1, newChildren2, 0, size2);
                NonLeaf leaf = new NonLeaf(m, newKeys2, newChildren2);
                if (this.parent == null) {
                    this.parent = new NonLeaf<>(m);
                }
                leaf.parent = this.parent;
                return parent.updateInsert((K) oldKeys[newSize], this, leaf);
            }
            return this;
        }


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

        protected Leaf(int m, Comparable[] keys, Object[] values) {
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
                    Comparable[] newKeys = new Comparable[size];
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
            return this;
        }

        private Node split() {
            if (this.size < m) {
                return this;
            } else {
                // 除以2操作
                int newSize = size >> 1;
                Comparable[] newKeys1 = new Comparable[newSize];
                Object[] newValue1 = new Object[newSize];
                System.arraycopy(this.keys, 0, newKeys1, 0, newSize);
                System.arraycopy(this.values, 0, newValue1, 0, newSize);
                this.keys = newKeys1;
                this.values = newValue1;
                int size2 = m - newSize;
                Comparable[] newKeys2 = new Comparable[size2];
                Object[] newValue2 = new Object[size2];
                System.arraycopy(this.keys, newSize, newKeys2, 0, size2);
                System.arraycopy(this.values, newSize, newValue2, 0, size2);
                Leaf leaf = new Leaf(m, newKeys2, newValue2);
                this.next = leaf;
                leaf.pre = this;
                if (this.parent == null) {
                    this.parent = new NonLeaf<>(m);
                }
                leaf.parent = this.parent;
                return parent.updateInsert((K) leaf.keys[0], this, leaf);
            }
        }

        @Override
        protected void delete(K k) {
            int index = getIndex(k);
            int newSize = size - 1;
            Comparable[] newKeys = new Comparable[newSize];
            Object[] newValue = new Object[newSize];
            System.arraycopy(this.keys, 0, newKeys, 0, index);
            System.arraycopy(this.values, newSize, newValue, 0, index);
            System.arraycopy(this.keys, index + 1, newKeys, index, newSize - index);
            System.arraycopy(this.values, index + 1, newValue, index, newSize - index);
            this.size = newSize;
            this.keys = newKeys;
            this.values = newValue;
        }

        @Override
        protected V search(K k) {
            int index = getIndex(k);
            return (V) values[index];
        }
    }


}
