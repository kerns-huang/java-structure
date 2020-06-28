package com.kerns.structure.tree;

import java.util.Arrays;

/**
 * 一个m阶的B+树包含的特性
 * 1.任意非叶子结点最多有M个子节点；且M>2；
 * 2.除根结点以外的非叶子结点至少有 M/2个子节点；
 * 3.根结点至少有2个子节点；
 * 4.除根节点外每个结点存放至少M/2和至多M个关键字；（至少2个关键字）
 * 5.非叶子结点的子树指针与关键字个数相同；
 * 6.所有结点的关键字：K[1], K[2], …, K[M]；且K[i] < K[i+1]；
 * 7.非叶子结点的子树指针P[i]，指向关键字值属于[K[i], K[i+1])的子树；
 * 8.所有叶子结点位于同一层；
 * 9.为所有叶子结点增加一个链指针；
 * 10.所有value都在叶子结点出现；
 * 扩展思考 B*树的实现，R树的实现
 * B+树有两种实现，最大值和最小值两种， 目前实现的是最大key链接
 *
 * @author xiaohei
 * @create 2020-04-21 下午2:23
 **/
public class BPlugsTree<K extends Comparable, V> {
    /**
     * 根节点
     */
    private Node<K, V> root;
    /**
     *
     */
    private Leaf<K, V> first;
    /**
     * 每个块包含多少数据
     */
    private int m;
    /**
     * 这个树的深度
     */
    private int h;
    /**
     * 树拥有的数据
     */
    private int size;


    public BPlugsTree(int m) {
        this.m = m;
        this.size = 0;
        root = new Leaf<K, V>(m);
    }

    public V get(K k) {
        return root.search(k);
    }

    public Integer size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 删除数据需要的考虑的问题
     * 1：删除k等于非叶子节点的值，删除的是叶子节点的最大值，需要把叶子节点的新的最大值赋值给父亲节点。
     * 2：当叶子节点小于m/2的时候，考虑从后面那个节点大于m/2 ，则借取（为什么不从前面那个借，因为父节点按照最大值索引，找前面借，会导致父节点的变换。）
     * 3：但儿子节点因为节点
     *
     * @param k
     * @return
     */
    public V delete(K k) {
        //TODO 递归删除
        if (k == null || size == 0) {
            return null;
        }
        size--;
        return root.delete(k, root);
    }

    /**
     * 添加新的节点
     *
     * @param k
     * @param v
     */
    public void insert(K k, V v) {
        //如果返回了新的root节点，则把原来的根节点替换为新的节点。
        Node node = root.insert(k, v);
        if (node != null) {
            this.root = node;
        }
        size++;

    }

    public void wirTree(){
        Node node=root;
        while(node instanceof NoneLeaf){
            NoneLeaf noneLeaf= (NoneLeaf)node;
            System.out.println(noneLeaf.toString());
            for(Node child:noneLeaf.children){
                System.out.print(child.toString()+"   ");
            }
            node=noneLeaf.children[noneLeaf.size-1];
        }
    }

    /**
     * 节点，叶子节点和非叶子节点共有方法抽取
     */
    abstract static class Node<K extends Comparable, V> {
        /**
         * 该节点的父亲节点
         */
        protected NoneLeaf<K, V> parent;
        /**
         * 一个块包含多个kv
         */
        protected Object[] keys;
        /**
         * 阶数，一个node最多包含几个数，最少不能少于m/2
         */
        protected int m;
        /**
         * 当前包含了多少数据
         */
        protected int size;

        protected Node(int m){
            this.m=m;
        }

        /**
         * 添加新的数据，有可能产生新的root节点
         *
         * @param k
         * @param v
         * @return 返回b树的root节，当没有新的父亲节点产生的时候，返回空
         */
        public abstract Node<K, V> insert(K k, V v);

        /**
         * 查找k 对应 的value
         *
         * @param k
         * @return
         */
        public abstract V search(K k);

        /**
         * 删除一条数据
         *
         * @param k
         * @return
         */
        public abstract V delete(K k, Node root);


        /**
         * 是否可以合并节点
         *
         * @param node
         * @return
         */
        protected boolean canMerge(Node<K, V> node) {
            return node != null && node.parent == this.parent && (node.size <= m / 2 || node.size <= 2);
        }

        /**
         * 是否可以借数据
         *
         * @param node
         * @return
         */
        protected boolean canBorrowEntry(Node<K, V> node) {
            return node != null && node.parent == this.parent && node.size > m / 2 && node.size > 2;
        }

        public String toString(){
            return Arrays.toString(keys);
        }

        protected abstract boolean hasChildren();
    }

    /**
     * 叶子节点 保存 索引字段和数据
     */
    static class Leaf<K extends Comparable, V> extends Node<K, V> {

        /**
         * 存入数据的key值
         */
        private Object[] values;
        /**
         * 前一个叶子节点
         */
        private Leaf<K, V> pre;
        /**
         * 后一个叶子节点
         */
        private Leaf<K, V> next;

        Leaf(int m) {
            super(m);
            this.keys = new Object[m];
            this.values = new Object[m];
        }

        /**
         * 添加新的节点，
         *
         * @param k
         * @param v
         * @return 返回b树的root节点，当没有新的父亲节点产生的时候，返回空
         */
        public Node<K, V> insert(K k, V v) {
            int i = 0;
            for (; i < size; ) {
                // 相等，说明key已经存在，不做处理，或者把value直接覆盖
                if (k.compareTo(keys[i]) == 0) {
                    return null;
                }
                //当k 小于某个排序的值的时候，说明它的位置就是i ，后面的数据需要往后
                if (k.compareTo(keys[i]) < 0) {
                    break;
                }
                i++;
            }

            this.size = size + 1;
            Object[] newKeys = new Object[size];
            Object[] newValues = new Object[size];
            newKeys[i] = k;
            newValues[i] = v;
            if (size == 1) {
                this.keys = newKeys;
                this.values = newValues;
                return null;
            }

            System.arraycopy(keys, 0, newKeys, 0, i);
            System.arraycopy(values, 0, newValues, 0, i);
            int copyLength = size - (i + 1);
            if (copyLength > 0) {
                System.arraycopy(keys, i, newKeys, i + 1, copyLength);
                System.arraycopy(values, i, newValues, i + 1, copyLength);
            }
            this.keys = newKeys;
            this.values = newValues;

            //当 数组的长度超过m的时候，需要分裂成两个Node节点。并把该节点的最大值生成一个父节点，如果还需要分裂，再次分裂
            if (size > m) {
                int newSize = size / 2;
                if (this.parent == null) {
                    this.parent = new NoneLeaf(m);
                }
                this.keys = new Object[m];
                this.values = new Object[m];
                this.size = newSize;
                /** 原来节点key value 重新赋值 */
                System.arraycopy(newKeys, 0, this.keys, 0, newSize);
                System.arraycopy(newValues, 0, this.values, 0, newSize);
                /** 前后节点赋值 */
                Leaf oldNext = this.next;
                Leaf<K, V> newLeaf = new Leaf<K, V>(m);
                this.next = newLeaf;
                //老的右节点赋值给新的接口的next属，因为 this < newLeaf < this.next
                newLeaf.next = oldNext;
                newLeaf.pre = this;
                /** 新节点key赋值 */
                newLeaf.size = newKeys.length - newSize;
                System.arraycopy(newKeys, newSize, newLeaf.keys, 0, newLeaf.size);
                System.arraycopy(newValues, newSize, newLeaf.values, 0, newLeaf.size);
                return parent.insert(this, newLeaf);
            }
            return null;
        }

        public V search(K k) {
            for (int i = 0; i < size; i++) {
                if (k.compareTo(keys[i]) == 0) {
                    return (V) values[i];
                }
            }
            return null;
        }

        public V delete(K k, Node root) {
            /**
             * TODO
             *  if 根目录
             *     直接删除数据，设置高度为0
             *  if size > m/2 :
             *     删除数据
             *  if (size <= m/2 || size==2)&&next.size > m/2 :
             *     从后面的节点借一个数据。
             *     设置当前节点的父亲节点的该节点的最大值。
             *  else if (size >= m/2 || size==2)&&pre.size > m/2 :
             *     从前面借一个数据。
             *     设置前一个节点的父亲节点前一个节点的最大值
             *  else:
             *     与前一个节点或者后面一个节点合并。
             */
            if (this == root || (size > m / 2 && size > 2)) {
                int i = 0;
                for (; i < size; i++) {
                    if (k.compareTo(keys[i]) == 0) {
                        return remove(i).getV();
                    }
                }
            } else {
                //当node的keys不足m/2时,找左节点要数据
                if (canBorrowEntry(pre)) {
                    int i = 0;
                    for (; i < size; i++) {
                        if (k.compareTo(keys[i]) == 0) {
                            //被删除的值
                            V removeValue = (V) this.values[i];
                            //把左node的最大一位移动这个Node的第一位
                            Entry<K, V> entry = pre.getAndRemoveLast();
                            pre.resetParentKey();
                            Object[] newKey = new Object[size];
                            Object[] newValue = new Object[size];
                            newKey[0] = entry.getK();
                            newValue[0] = entry.getV();
                            //第一位已经被左边节点占，所以新数组从第一位开始拷贝
                            System.arraycopy(this.keys, 0, newKey, 1, i);
                            System.arraycopy(this.values, 0, newValue, 1, i);
                            if (size > i + 1) {
                                int len = size - i - 1;
                                System.arraycopy(this.keys, i, newKey, i + 1, len);
                                System.arraycopy(this.values, i, newValue, i + 1, len);
                            }
                            return removeValue;
                        }
                    }
                } else if (canBorrowEntry(next)) {
                    int i = 0;
                    for (; i < size; i++) {
                        if (k.compareTo(keys[i]) == 0) {
                            //被删除的值
                            V removeValue = (V) this.values[i];
                            //把左node的最大一位移动这个Node的第一位
                            Entry<K, V> entry = next.getAndRemoveFirst();
                            this.resetParentKey();
                            Object[] newKey = new Object[size];
                            Object[] newValue = new Object[size];
                            newKey[size - 1] = entry.getK();
                            newValue[size - 1] = entry.getV();
                            //第一位已经被左边节点占，所以新数组从第一位开始拷贝
                            System.arraycopy(this.keys, 0, newKey, 0, i);
                            System.arraycopy(this.values, 0, newValue, 0, i);
                            if (size > i + 1) {
                                int len = size - i - 1;
                                System.arraycopy(this.keys, i, newKey, i, len);
                                System.arraycopy(this.values, i, newValue, i, len);
                            }
                            return removeValue;
                        }
                    }
                } else if (canMerge(pre)) {
                    int i = 0;
                    for (; i < size; i++) {
                        if (k.compareTo(keys[i]) == 0) {
                            //先删除需要删除的节点
                            remove(i);
                            Object[] keys = new Object[pre.size + this.size];
                            System.arraycopy(pre.keys, 0, keys, 0, pre.size);
                            System.arraycopy(this.keys, 0, keys, pre.size, this.size);
                            Object[] values = new Object[pre.size + this.size];
                            System.arraycopy(pre.values, 0, values, 0, pre.size);
                            System.arraycopy(this.values, 0, values, pre.size, this.size);
                            this.keys = keys;
                            this.values = values;
                            /**
                             * 和前节点合并的时候，把前节点的数据放置到当前节点，这样不用改变当前节点对应父节点的值
                             */
                            Leaf<K, V> removeNode = this.pre;
                            if (pre.pre != null) {
                                pre.pre.next = this;
                                this.pre = pre.pre;
                            }
                            //父类对象删除对删除node的引用
                            if (parent != null) {
                                parent.remove(removeNode);
                            }
                            removeNode.pre = null;
                            removeNode.next = null;
                            removeNode = null;

                        }
                    }
                } else if (canMerge(next)) {
                    int i = 0;
                    for (; i < size; i++) {
                        if (k.compareTo(keys[i]) == 0) {
                            //删除节点
                            remove(i);
                            //和前节点合并
                            Object[] keys = new Object[next.size + this.size];
                            System.arraycopy(this.keys, 0, keys, 0, size);
                            System.arraycopy(next.keys, 0, keys, size, next.size);
                            Object[] values = new Object[next.size + this.size];
                            System.arraycopy(this.values, i, values, 0, size);
                            System.arraycopy(next.values, 0, values, size, next.size);
                            this.next.keys = keys;
                            this.next.values = values;
                            //与兄弟节点的操作
                            Leaf removeNode = this;
                            if (next != null) {
                                next.pre = this.pre;
                                if (this.pre != null) {
                                    this.pre.next = this.next;
                                }
                            }
                            //父类对象操作 删除当前节点的应用
                            if (parent != null) {
                                parent.remove(this);
                            }
                            this.pre = null;
                            this.next = null;

                        }
                    }
                }

            }


            return null;
        }

        protected boolean hasChildren() {
            return false;
        }

        /**
         * 删除数据
         *
         * @param i
         * @return
         */
        private Entry<K, V> remove(Integer i) {
            Entry<K, V> entry = new Entry(this.keys[i], this.values[i]);
            //减少数据总数
            size--;
            Object[] newKey = new Object[size];
            Object[] newValue = new Object[size];
            System.arraycopy(this.keys, 0, newKey, 0, i);
            System.arraycopy(this.values, 0, newValue, 0, i);
            int len = size - i;
            System.arraycopy(this.keys, i + 1, newKey, i, len);
            System.arraycopy(this.values, i + 1, newValue, i, len);
            this.keys = newKey;
            this.values = newValue;
            return entry;
        }

        /**
         * 重置中间节点的对应这个节点的关键字
         */
        public void resetParentKey() {
            if (this.parent != null) {
                parent.resetKey(this);
            }
        }

        /**
         * 获取最后一个节点数据给后面那个节点
         *
         * @return
         */
        private Entry<K, V> getAndRemoveLast() {
            return remove(size - 1);
        }

        /**
         * 获取next节点的第一个数据给前面那个节点。
         *
         * @return
         */
        private Entry<K, V> getAndRemoveFirst() {
            return remove(0);
        }
    }

    /**
     * 获取 kv 对象
     *
     * @param <K>
     * @param <V>
     */
   static class Entry<K, V> {
        private K k;

        private V v;

        public Entry(K k, V v) {
            this.k = k;
            this.v = v;
        }

        public K getK() {
            return k;
        }

        public V getV() {
            return v;
        }
    }

    /**
     * 非叶子节点
     */
    static class NoneLeaf<K extends Comparable, V> extends Node<K, V> {
        /**
         * 子节点，可能是叶子节点，也有可能是非叶子节点
         */
        private Node<K, V>[] children;

        public NoneLeaf(int m) {
            super(m);
            this.keys = new Object[m];
            this.children = new Node[m];
        }

        /**
         * 当 子节点的最大值改变之后需要递归变更父节点的关键字
         *
         * @param child
         */
        private void resetKey(Node child) {
            for (int i = 0; i < size; i++) {
                if (children[i] == child) {
                    this.keys[i] = child.keys[child.size - 1];
                }
                if (i == size - 1) {
                    //如果更新的最后一个数值，递归更新父亲节点
                    resetKey(this);
                }
            }
        }

        /**
         * 删除子节点，当两个节点合并的时，会去掉对一个node的引用
         *
         * @param child
         */
        private void remove(Node child) {
            for (int i = 0; i < size; i++) {
                if (children[i] == child) {
                    remove(i);
                    //TODO 删除之后，判断是否需要合并
                }
            }
        }

        /**
         * b+ 树按照道理非叶子节点其实没有前后节点的概念？
         * @param root
         */
        private void updateRemove(Node root) {
            //如果size 大于 m/2 不做处理
            if (this.size > 2 && this.size > m / 2) {
                return;
            }

        }

        /**
         * 删除数据，生成一个新的数组放新的数据组
         *
         * @param i
         */
        private void remove(int i) {
            size--;
            Object[] newKey = new Object[size];
            Node[] newValue = new Node[size];
            System.arraycopy(this.keys, 0, newKey, 0, i);
            System.arraycopy(this.children, 0, newValue, 0, i);
            int len = size - i;
            System.arraycopy(this.keys, i + 1, newKey, i, len);
            System.arraycopy(this.children, i + 1, newValue, i, len);
            this.keys = newKey;
            this.children = newValue;
        }


        /**
         * @param left  左节点 是本来的节点，所以children的指向不会变
         * @param right 大节点 是新的节点，所以child的指向需要新增
         *              为什么叫left k 而不是加right，因为 right最大值已经是插入的时候做了一次替换，所以没有必要再加。
         */
        private Node<K, V> insert(Node left, Node right) {
            K k = (K) left.keys[left.size - 1];
            if (this.size == 0) {
                children = new Node[m];
                children[0] = left;
                children[1] = right;
                left.parent = this;
                right.parent = this;
                keys = new Object[2];
                keys[0] = k;
                keys[1] = right.keys[right.size - 1];
                this.size += 2;
                return this;
            }
            int i = 0;
            for (; i < size; i++) {
                // 相等，说明key已经存在，不做处理，或者把value直接覆盖
                if (k.compareTo(keys[i]) == 0) {
                    return null;
                }
                //当k 小于某个排序的值的时候，说明它的位置就是i ，后面的数据需要往后
                if (k.compareTo(keys[i]) < 0) {
                    break;
                }
            }
            this.size = size + 1;
            Object[] newKeys = new Object[size];
            Node[] newChildren = new Node[size + 1];
            newKeys[i] = k;
            //设置新节点的父亲节点为该节点
            right.parent = this;
            newChildren[i + 1] = right;
            if (size == 1) {
                keys = newKeys;
                return null;
            }
            System.arraycopy(keys, 0, newKeys, 0, i);
            System.arraycopy(children, 0, newChildren, 0, i + 1);
            int copyLength = size - (i + 1);
            if (copyLength > 0) {
                System.arraycopy(keys, i, newKeys, i + 1, copyLength);
            }
            copyLength = size - (i + 2);
            if (copyLength > 0) {
                System.arraycopy(children, i + 1, newChildren, i + 2, copyLength);
            }
            this.keys = newKeys;
            this.children = newChildren;
            //当 数组的长度超过m的时候，需要分裂成两个Node节点。并把该节点的最大值生成一个父节点，如果还需要分裂，再次分裂
            if (size > m) {
                int newSize = size / 2;
                if (this.parent == null) {
                    this.parent = new NoneLeaf(m);
                }
                this.keys = new Object[m];
                this.size = newSize;
                System.arraycopy(newKeys, 0, this.keys, 0, newSize);
                System.arraycopy(newChildren, 0, this.children, 0, newSize + 1);
                NoneLeaf<K, V> newLeaf = new NoneLeaf<K, V>(m);
                newLeaf.size = newKeys.length - newSize;
                System.arraycopy(newKeys, newSize, newLeaf.keys, 0, newLeaf.size);
                System.arraycopy(newChildren, newSize, newLeaf.children, 0, newLeaf.size);
                return parent.insert(this, newLeaf);
            }
            return null;
        }

        public Node<K, V> insert(K k, V v) {
            /**
             * 首先查找k 该k 是否是小于等于keys[i],
             * 是：就取child[i] ，一致递归到leaf 节点，进行实际插入操作。
             */
            for (int i = 0; i < size; i++) {
                if (k.compareTo(keys[i]) <= 0) {
                    return children[i].insert(k, v);
                }
            }
            /**
             * key的值为子节点的最大值
             * 如果该值比最大值还要大，替换最大值为插入值，然后插入最大的子节点。
             */
            keys[size - 1] = k;
            return children[size - 1].insert(k, v);
        }

        /**
         * 典型的二叉树查找规则，从上面之下，和跳表的规则类似
         *
         * @param k
         * @return
         */
        public V search(K k) {
            for (int i = 0; i < size; i++) {
                //从最小值开始比较
                if (k.compareTo(keys[i]) <= 0) {
                    return children[i].search(k);
                }
            }
            return children[size-1].search(k);
        }

        /**
         * 删除的时候需要注意的问题，删除最大值，因为父节点是通过最大值来对应查找子节点，如果不做同步，有可能导致查找不准确
         * 所以在这个情况下应该先删除子节点的最大值，然后把新的最大值传递给父节点。
         *
         * @param k
         * @return
         */
        public V delete(K k, Node root) {
            for (int i = 0; i < size; i++) {
                //从最小值开始比较
                if (k.compareTo(keys[i]) <= 0) {
                    return children[i].delete(k, root);
                }
            }
            return null;
        }

        protected boolean hasChildren() {
            return true;
        }
    }


}
