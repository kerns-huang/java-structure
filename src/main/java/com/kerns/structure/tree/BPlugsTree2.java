package com.kerns.structure.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 标准的b+树基于内存
 * 参考动图
 * https://www.cs.usfca.edu/~galles/visualization/BPlusTree.html
 */
public class BPlugsTree2<K extends Comparable, V> {

    private Node<K, V> root;
    /**
     * 包含的数据
     */
    private int size;

    private int m;

    private static final int D = 2;

    public BPlugsTree2(int m) {
        root = new Leaf<>(m);
        this.m = m;
    }

    /**
     * 获取树包含的数据
     *
     * @return
     */
    public Integer size() {
        return root.getKeyCount();
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
        while (node instanceof NonLeaf) {
            //非叶子节点
            int index = node.getIndex(k);
            if (index < 0) {
                index = -index - 1;
            }
            node = ((NonLeaf) node).children[index];
        }
        // 永远返回的是根节点
        Node newNode = node.insert(k, v);
        //后期重新构建树，计算总的长，先直接通过加减做
        if (newNode != null) {
            //更新节点，默认情况下会有多线程进程，h2 使用compare and set 实现
            root = newNode;
        }
    }


    /**
     * 查找数据
     *
     * @param k
     * @return
     */
    public V get(K k) {
        return root.search(k);
    }

    /**
     * 删除数据
     *
     * @param k
     */
    public void delete(K k) {
        if (root instanceof Leaf) {
            Leaf<K, V> leafRoot = (Leaf<K, V>) root;
            leafRoot.delete(k);
        } else {
            List<Node<K, V>> path = new ArrayList<Node<K, V>>();
            List<Integer> indexInParentPath = new ArrayList<Integer>();
            Node<K, V> tempNode = root;
            while (tempNode instanceof NonLeaf) {
                path.add(tempNode);
                NonLeaf<K, V> nonLeaf = (NonLeaf<K, V>) tempNode;
                int i = nonLeaf.getIndex(k);
                i = i < 0 ? ~i : i + 1;
                indexInParentPath.add(i); // ith in parent's children - ArrayList
                tempNode = nonLeaf.children[i];
            }

            Leaf<K, V> tempLeafNode = (Leaf<K, V>) tempNode;
            int indexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
            tempLeafNode.delete(k);
            //叶子节点小于阀值
            if (!tempLeafNode.isUnderflowed()) {//tempLeafNode is not underflowed after delete
                indexInParent = -1;
            } else {
                NonLeaf<K, V> parent = (NonLeaf<K, V>) path.get(path.size() - 1);
                if (indexInParent == 0) {
                    indexInParent = handleLeafNodeUnderflow(1, tempLeafNode, (Leaf<K, V>) parent.children[indexInParent + 1],
                            parent, indexInParentPath);
                } else {
                    indexInParent = handleLeafNodeUnderflow(0, (Leaf<K, V>) parent.children[indexInParent - 1], tempLeafNode,
                            parent, indexInParentPath);
                }
            }
            while (indexInParent != -1 && !path.isEmpty()) {
                NonLeaf<K, V> tempIndexNode = (NonLeaf<K, V>) path.remove(path.size() - 1);
                indexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
                if (!tempIndexNode.isUnderflowed()) {
                    indexInParent = -1;
                } else {
                    NonLeaf<K, V> parent = (NonLeaf<K, V>) path.get(path.size() - 1);
                    if (indexInParent == 0) {
                        indexInParent = handleNonLeafUnderflow(1, tempIndexNode,
                                (NonLeaf<K, V>) parent.children[indexInParent + 1],
                                parent, indexInParentPath);
                    } else {
                        indexInParent = handleNonLeafUnderflow(0,
                                (NonLeaf<K, V>) parent.children[indexInParent - 1],
                                tempIndexNode, parent, indexInParentPath);
                    }
                }
            }
        }
    }

    /**
     * 叶子节点的借取
     *
     * @param sibling
     * @param leftLeaf
     * @param rightLeaf
     * @param parent
     * @param indexInParentPath
     * @return
     */
    public int handleLeafNodeUnderflow(int sibling, Leaf<K, V> leftLeaf, Leaf<K, V> rightLeaf,
                                       NonLeaf<K, V> parent, List<Integer> indexInParentPath) {
        if (sibling == 1) {
            //找右边兄弟节点借取
            if (!rightLeaf.isUnderflowed()) {
                int count = (rightLeaf.size - 2 + 1) / 2;
                if (count > 0) {
                    leftLeaf.borrowFromRight(rightLeaf, count);
                }
                int leftIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
                parent.keys[leftIndexInParent] = rightLeaf.keys[0];
                return -1;
            } else {
                //TODO 合并兄弟节点
                merge((Leaf<K, V>) leftLeaf, (Leaf<K, V>) rightLeaf);
                int leftIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
                parent.deleteKey(leftIndexInParent);
                if (parent.size == 0) {
                    root = rightLeaf;
                    return -1;
                }
                indexInParentPath.remove(indexInParentPath.size() - 1);
                if (indexInParentPath.size() == 0) {
                    return -1;
                } else {
                    return indexInParentPath.get(indexInParentPath.size() - 1);
                }
            }
        } else {
            // 向兄弟节点借取数据
            if (leftLeaf.isUnderflowed()) {
                int count = (leftLeaf.size - 2 + 1) - (leftLeaf.size - 2 + 1) / 2;
                if (count > 0) {
                    rightLeaf.borrowFromLeft(leftLeaf, count);
                }
                int rightIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
                parent.keys[rightIndexInParent - 1] = rightLeaf.keys[0];
                return -1;
            } else {//merge
                //合并兄弟节点
                merge(leftLeaf, rightLeaf);
                int rightIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
                parent.deleteKey(rightIndexInParent - 1);
                if (parent.size == 0) {
                    root = rightLeaf;
                    return -1;
                }

                indexInParentPath.remove(indexInParentPath.size() - 1);
                if (indexInParentPath.size() == 0) {
                    return -1;
                } else {
                    return indexInParentPath.get(indexInParentPath.size() - 1);
                }

            }
        }
    }

    /**
     * 两个节点合并成一个节点
     *
     * @param leftLeaf
     * @param rightLeaf
     */
    private void merge(Leaf<K, V> leftLeaf, Leaf<K, V> rightLeaf) {
        int newSize = rightLeaf.size + leftLeaf.size;
        Comparable[] newKeys = new Comparable[newSize];
        Objects[] newValues = new Objects[newSize];
        System.arraycopy(leftLeaf.keys, 0, newKeys, 0, leftLeaf.size);
        System.arraycopy(rightLeaf.keys, 0, newKeys, leftLeaf.size, rightLeaf.size);
        System.arraycopy(leftLeaf.values, 0, newValues, 0, leftLeaf.size);
        System.arraycopy(rightLeaf.values, 0, newValues, leftLeaf.size, rightLeaf.size);
        rightLeaf.keys = newKeys;
        rightLeaf.values = newValues;
    }


    /**
     * 合并或者借取
     *
     * @param left   : the smaller node
     * @param right  : the bigger node
     * @param parent : their parent index node
     * @return the splitkey position in parent if merged so that parent can
     * delete the splitkey later on. -1 otherwise
     */
    public int handleNonLeafUnderflow(int sibling, NonLeaf<K, V> left, NonLeaf<K, V> right,
                                      NonLeaf<K, V> parent, List<Integer> indexInParentPath) {
        if (sibling == 1) {
            if (right.size >= D + 1) {
                //从右节点借取对象
                int leftIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
                left.append((K) parent.keys[leftIndexInParent], (NonLeaf) right.children[0]);
                int count = (right.size - 2 + 1) / 2 - 1;
                left.borrowFromRight(right, count);
                parent.insertKey(leftIndexInParent, (K) right.keys[0]);
                right.deleteKey(0);
                return -1;
            } else {
                //合并节点
                int leftIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
                right.insertKey(0, (K) parent.keys[leftIndexInParent]);
                right.insertChild(0, left.children[left.children.length - 1]);
                //把左节点合并到右节点
                right.appendLeft(left);
                parent.deleteKey(leftIndexInParent);
                if (parent.keys.length == 0) {
                    root = right;
                    return -1;
                }
                indexInParentPath.remove(indexInParentPath.size() - 1);
                if (indexInParentPath.size() == 0) {
                    return -1;
                } else {
                    return indexInParentPath.get(indexInParentPath.size() - 1);
                }
            }
        } else {
            //找左节点借取数据
            if (left.keys.length >= D + 1) {
                int rightIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
                right.insertKey(0, (K) parent.keys[rightIndexInParent - 1]);
                right.insertChild(0, left.children[left.children.length - 1]);
                left.deleteChild(left.children.length - 1);
                int count = 1;
                while (count <= (left.keys.length - D + 1)
                        - (left.keys.length - D + 1) / 2 - 1) {
                    int last = left.keys.length - 1;
                    right.insertKey(0, (K) left.keys[last]);
                    right.insertChild(0, left.children[last]);
                    left.deleteKey(last);
                    count++;
                }
                parent.insertKey(rightIndexInParent - 1, (K) left.keys[left.keys.length - 1]);
                parent.deleteKey(left.keys.length - 1);
                return -1;
            } else {
                //合并数据
                int rightIndexInParent = indexInParentPath.get(indexInParentPath.size() - 1);
                right.insertKey(0, (K) parent.keys[rightIndexInParent - 1]);
                right.insertChild(0, left.children[left.children.length - 1]);
                right.appendLeft(left);
                parent.deleteByIndex(rightIndexInParent - 1);
                if (parent.keys.length == 0) {
                    root = right;
                    return -1;
                }
                indexInParentPath.remove(indexInParentPath.size() - 1);
                if (indexInParentPath.size() == 0) {
                    return -1;
                } else {
                    return indexInParentPath.get(indexInParentPath.size() - 1);
                }
            }
        }
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
            this.keys = new Comparable[m];
        }

        public abstract int getKeyCount();

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

        protected void deleteKey(int index) {
            Comparable[] newKeys = new Comparable[this.size - 1];
            System.arraycopy(this.keys, 0, newKeys, 0, index);
            System.arraycopy(this.keys, index + 1, newKeys, index, this.size
                    - index - 1);
        }

        /**
         *
         */
        protected abstract V search(K k);

        /**
         * 是否是小于最小值
         *
         * @return
         */
        public boolean isOverflowed() {
            return size > m;
        }

        /**
         * 是否是小于最小值
         *
         * @return
         */
        public boolean isUnderflowed() {
            return size < (m / 2 - 1);
        }

        /**
         * 中间查找,存的数据越多，效率越高,
         * 这个查找是从H2里面直接抄过来的，说是在的，确实比没脑的for循环要快很多，如果是专门做中间件，确实应该可以
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
            return ~low;//等于-(low+1)
        }
    }

    static class Pair<K extends Comparable, V> {
        private K k;

        private V value;

        public Pair(K k, V value) {
            this.k = k;
            this.value = value;
        }

        public K getK() {
            return k;
        }

        public void setK(K k) {
            this.k = k;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
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

        @Override
        public int getKeyCount() {
            int keyCount = 0;
            for (int i = 0; i <= size; i++) {
                keyCount += this.children[i].getKeyCount();
            }
            return keyCount;
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

        private void insertKey(int index, K k) {
            int newSize = this.size + 1;
            Comparable[] newKeys = new Comparable[newSize];
            System.arraycopy(this.keys, 0, newKeys, 0, index);
            System.arraycopy(this.keys, index, newKeys, index + 1, this.size - index);
            newKeys[index] = k;
            this.size = newSize;
            this.keys = newKeys;
        }

        private void insertChild(int index, Node child) {
            int newSize = this.children.length + 1;
            Node[] newChildren = new Node[newSize];
            System.arraycopy(this.children, 0, newChildren, 0, index);
            System.arraycopy(this.children, index, newChildren, index + 1, this.size - index);
            newChildren[index] = child;
            this.children = newChildren;
        }

        private void append(K k, NonLeaf node) {
            int newSize = this.size + 1;
            Comparable[] newKeys = new Comparable[newSize];
            Node[] newChildren = new Node[newSize];
            System.arraycopy(this.keys, 0, newKeys, 0, this.size);
            System.arraycopy(this.children, 0, newChildren, 0, this.size);
        }

        protected void deleteChild(int index) {
            int newChildSize = children.length - 1;
            Node[] newChildren = new Node[newChildSize];
            System.arraycopy(this.children, 0, newChildren, 0, index);
            System.arraycopy(this.children, index + 1, newChildren, index, newChildSize - index);
            this.children = children;
        }

        @Override
        protected void delete(K k) {

        }

        protected void deleteByIndex(int index) {
            super.deleteKey(index);
            Node[] newChildren = new Node[this.size - 1];
            System.arraycopy(this.children, 0, newChildren, 0, index);
            System.arraycopy(this.children, index + 1, newChildren, index, this.size
                    - index - 1);
        }

        @Override
        protected V search(K k) {
            int index = getIndex(k);
            // 递归查找，一直查找到叶子节点。
            return (V) children[index].search(k);
        }

        /**
         * 从右边节点借数据
         *
         * @param right
         * @param range
         */
        void borrowFromRight(NonLeaf right, int range) {

        }

        /**
         * 从左边节点借数据
         *
         * @param left
         * @param range
         */
        void borrowFromLeft(NonLeaf left, int range) {

        }

        void appendLeft(NonLeaf left) {
            int newSize = this.size + left.size;
            Comparable[] newKeys = new Comparable[newSize];
            Node[] newChildren = new Node[newSize];
            System.arraycopy(left.keys, 0, newKeys, 0, left.size);
            System.arraycopy(left.children, 0, newChildren, 0, left.size);
            System.arraycopy(this.keys, 0, newKeys, left.size, this.size);
            System.arraycopy(this.children, 0, newChildren, left.size, this.size);
            this.size = newSize;
            this.keys = newKeys;
            this.children = newChildren;
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
                int i = getIndex(k);
                if (i < 0) {
                    i = -i - 1;
                }
                Comparable[] newKeys = new Comparable[size + 1];
                Node[] newChildren = new Node[size + 2];
                newKeys[i] = k;
                newChildren[i + 1] = right;
                System.arraycopy(this.keys, 0, newKeys, 0, i);
                System.arraycopy(this.children, 0, newChildren, 0, i + 1);
                System.arraycopy(this.keys, i, newKeys, i, size - i);
                System.arraycopy(this.children, i, newChildren, i + 1, size - i);
                this.keys = newKeys;
                this.children = newChildren;
                size++;
                return split();
            }
        }

        private Node split() {
            if (this.size >= m) {
                // 除以2操作
                Comparable[] oldKeys = this.keys;
                Node[] oldChildren = this.children;
                int newSize = size >> 1;
                Comparable[] newKeys1 = new Comparable[newSize];
                Node[] newChildren1 = new Node[newSize + 1];
                System.arraycopy(oldKeys, 0, newKeys1, 0, newSize);
                System.arraycopy(oldChildren, 0, newChildren1, 0, newSize + 1);
                this.keys = newKeys1;
                this.children = newChildren1;
                this.size = newSize;
                int size2 = m - newSize - 1;
                Comparable[] newKeys2 = new Comparable[size2];
                Node[] newChildren2 = new Node[size2 + 1];
                System.arraycopy(oldKeys, newSize + 1, newKeys2, 0, size2);
                System.arraycopy(oldChildren, newSize + 1, newChildren2, 0, size2 + 1);
                NonLeaf leaf = new NonLeaf(m, newKeys2, newChildren2);
                if (this.parent == null) {
                    this.parent = new NonLeaf<>(m);
                }
                //修改父节点
                for (Node node : newChildren2) {
                    node.parent = leaf;
                }
                leaf.parent = this.parent;
                return parent.updateInsert((K) oldKeys[newSize], this, leaf);
            }
            return null;
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

        @Override
        public int getKeyCount() {
            return size;
        }

        protected Leaf(int m, Comparable[] keys, Object[] values) {
            super(m);
            this.size = keys.length;
            this.keys = keys;
            this.values = values;
        }

        @Override
        protected Node insert(K k, V v) {
            int i = getIndex(k);
            if (i < 0) {
                i = -i - 1;
            }
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


        private Node split() {
            if (this.size < m) {
                return null;
            } else {
                // 除以2操作
                int newSize = size >> 1;
                Comparable[] newKeys1 = new Comparable[newSize];
                Object[] newValue1 = new Object[newSize];
                System.arraycopy(this.keys, 0, newKeys1, 0, newSize);
                System.arraycopy(this.values, 0, newValue1, 0, newSize);
                int size2 = m - newSize;
                Comparable[] newKeys2 = new Comparable[size2];
                Object[] newValue2 = new Object[size2];
                System.arraycopy(this.keys, newSize, newKeys2, 0, size2);
                System.arraycopy(this.values, newSize, newValue2, 0, size2);
                Leaf leaf = new Leaf(m, newKeys2, newValue2);
                this.next = leaf;
                leaf.pre = this;
                this.keys = newKeys1;
                this.values = newValue1;
                this.size = newSize;
                if (this.parent == null) {
                    this.parent = new NonLeaf<>(m);
                }
                leaf.parent = this.parent;
                return parent.updateInsert((K) leaf.keys[0], this, leaf);
            }
        }

        /**
         * 从右边节点借数据
         *
         * @param right
         * @param range
         */
        void borrowFromRight(Leaf right, int range) {
            int rightNewSize = this.size - range;
            Comparable[] rightNewKeys = new Comparable[this.size - range];
            Objects[] rightNewValues = new Objects[this.size - range];
            System.arraycopy(right.keys, range, rightNewKeys, 0, rightNewSize);
            System.arraycopy(right.values, range, rightNewValues, 0, rightNewSize);
            int newSize = this.size + range;
            Comparable[] newKeys = new Comparable[newSize];
            Objects[] newValues = new Objects[newSize];
            System.arraycopy(this.keys, 0, newKeys, 0, this.size);
            System.arraycopy(this.values, 0, newValues, 0, this.size);
            System.arraycopy(right.keys, 0, newKeys, this.size, range);
            System.arraycopy(right.values, 0, newValues, this.size, range);
            this.keys = rightNewKeys;
            this.values = rightNewValues;
            this.size = rightNewSize;
            right.keys = newKeys;
            right.values = newValues;
            right.size = newSize;
        }

        /**
         * 从左边节点借range个元素数据
         *
         * @param left
         * @param range
         */
        protected void borrowFromLeft(Leaf left, int range) {
            int leftNewSize = left.size - range;
            Comparable[] leftNewKeys = new Comparable[leftNewSize];
            Objects[] leftNewValues = new Objects[leftNewSize];
            System.arraycopy(left.keys, 0, leftNewKeys, 0, leftNewSize);
            System.arraycopy(left.values, 0, leftNewValues, 0, leftNewSize);
            int newSize = left.size + range;
            Comparable[] newKeys = new Comparable[newSize];
            Objects[] newValues = new Objects[newSize];
            System.arraycopy(left.keys, leftNewSize, newKeys, 0, range);
            System.arraycopy(left.values, leftNewSize, newValues, 0, range);
            System.arraycopy(this.keys, 0, newKeys, range, this.size);
            System.arraycopy(this.values, 0, newValues, range, this.size);
            left.keys = newKeys;
            left.values = leftNewValues;
            left.size = leftNewSize;
            this.keys = newKeys;
            this.values = newValues;
            this.size = newSize;
        }

        @Override
        protected void delete(K k) {
            int i = getIndex(k);
            if (i >= 0) {
                Comparable[] newKey = new Comparable[this.size - 1];
                Object[] newValue = new Object[this.size - 1];
                copyExclude(newKey, newValue, i);
                this.size--;
                this.keys = newKey;
                this.values = newValue;
            }
        }

        protected void deleteKey(int index) {
            super.deleteKey(index);
            Object[] newValues = new Object[this.size - 1];
            System.arraycopy(this.values, 0, newValues, 0, index);
            System.arraycopy(this.values, index + 1, newValues, index, this.size
                    - index - 1);
        }


        private void copyExclude(Comparable[] newKeys, Object[] newValue, int exclude) {
            System.arraycopy(this.keys, 0, newKeys, 0, exclude);
            System.arraycopy(this.values, 0, newValue, 0, exclude);
            System.arraycopy(this.keys, exclude + 1, newKeys, exclude, this.size - exclude - 1);
            System.arraycopy(this.values, exclude + 1, newValue, exclude, this.size - exclude - 1);
        }


        @Override
        protected V search(K k) {
            int index = getIndex(k);
            return (V) values[index];
        }
    }


}
