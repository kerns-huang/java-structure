package com.kerns.structure.tree;

import com.oracle.webservices.internal.api.databinding.DatabindingMode;

/**
 * 红黑数的实现
 * 创建人:kerns
 * 创建时间: 2019/6/19 2:01 PM
 * 红黑树的特性
 * (1) 每个节点或者是黑色，或者是红色。
 * (2) 根节点是黑色。
 * (3) 每个叶子节点是黑色。 [注意：这里叶子节点，是指为空的叶子节点！]
 * (4) 如果一个节点是红色的，则它的子节点必须是黑色的。
 * (5) 从一个节点到该节点的子孙节点的所有路径上包含相同数目的黑节点。
 **/

public class RedBlackTree<T extends Comparable<T>> {

    private RBTreeNode<T> root;

    private static final boolean RED   = false;
    private static final boolean BLACK = true;
    public static class RBTreeNode<T extends Comparable<T>> {
      private boolean color;
      private T key;
      private RBTreeNode<T> left;
      private RBTreeNode<T> right;
      private RBTreeNode<T> parent;

        public RBTreeNode(boolean color, T key, RBTreeNode<T> left, RBTreeNode<T> right, RBTreeNode<T> parent) {
            this.color = color;
            this.key = key;
            this.left = left;
            this.right = right;
            this.parent = parent;
        }
    }

    /**
     * 插入一条数据
     * @param key
     */
    public void insert(T key) {
        RBTreeNode<T> node=new RBTreeNode<T>(RED,key,null,null,null);
        // 如果新建结点失败，则返回。
        if (node != null)
            insert(node);
    }


    /*
     * 对红黑树的节点(x)进行左旋转，意味着x做为左节点存在
     *
     * 左旋示意图(对节点x进行左旋)：
     *      px                              px
     *     /                               /
     *    x                               y
     *   /  \      --(左旋)-.           / \                #
     *  lx   y                          x  ry
     *     /   \                       /  \
     *    ly   ry                     lx  ly
     *
     *
     */
    private void leftRotate(RBTreeNode<T> x)
    {
        RBTreeNode y= x.right; //先把右节点拿到
        // 把ly挂载x
        x.right=y.left;//把x节点的右节点换成ly
        if (y.left != null)
            y.left.parent = x;
        // 将 “x的父亲” 设为 “y的父亲”
        y.parent=x.parent;

        // 如果 “x的父亲” 是空节点，则将y设为根节点
        if (x.parent == null) {
            this.root = y;            // 如果 “x的父亲” 是空节点，则将y设为根节点
        } else {
            if (x.parent.left == x)
                x.parent.left = y;    // 如果 x是它父节点的左孩子，则将y设为“x的父节点的左孩子”
            else
                x.parent.right = y;    // 如果 x是它父节点的左孩子，则将y设为“x的父节点的左孩子”
        }
        // 将 “x” 设为 “y的左孩子”
        y.left = x;
        // 将 “x的父节点” 设为 “y”
        x.parent = y;
    }
    /*
     * 对红黑树的节点(y)进行右旋转，y 节点成为右节点
     *
     * 右旋示意图(对节点y进行左旋)：
     *            py                               py
     *           /                                /
     *          y                                x
     *         /  \      --(右旋)-.            /  \                     #
     *        x   ry                           lx   y
     *       / \                                   / \                   #
     *      lx  rx                                rx  ry
     *
     */
    private void rightRotate(RBTreeNode<T> y)
    {
        RBTreeNode<T> x = y.left;

        // 将 “x的右孩子” 设为 “y的左孩子”；
        // 如果"x的右孩子"不为空的话，将 “y” 设为 “x的右孩子的父亲”
        y.left = x.right;
        if (x.right != null)
            x.right.parent = y;

        // 将 “y的父亲” 设为 “x的父亲”
        x.parent = y.parent;

        if (y.parent == null) {
            this.root = x;            // 如果 “y的父亲” 是空节点，则将x设为根节点
        } else {
            if (y == y.parent.right)
                y.parent.right = x;    // 如果 y是它父节点的右孩子，则将x设为“y的父节点的右孩子”
            else
                y.parent.left = x;    // (y是它父节点的左孩子) 将x设为“x的父节点的左孩子”
        }

        // 将 “y” 设为 “x的右孩子”
        x.right = y;

        // 将 “y的父节点” 设为 “x”
        y.parent = x;

    }

    private void insert(RBTreeNode<T> node) {
       RBTreeNode root= this.root;
       if(root==null)
       {
           this.root=node;
           return;
       }

       RBTreeNode temp=root;
       //找到对应的挂载节点
       while(temp!=null)
       {
           int cmp=temp.key.compareTo(node.key);

           if(cmp>0)//如果大于0 ，通过左节点去比较
           {
             temp=temp.left;
           }
           else
           {
               temp=temp.right;
           }
       }

       node.parent=temp;
       int cmp=temp.key.compareTo(node.key);
       if(cmp>0)
       {
           temp.left=node;
       }
       else
       {
           temp.right=node;
       }
       node.color=RED;
       // 重新平衡，染色
        insertFixUp(node);
    }

    private boolean isRed(RBTreeNode<T> node)
    {
        return RED==node.color;
    }

    public RBTreeNode parentOf(RBTreeNode<T> node)
    {
        return node.parent;
    }

    public void setBlack(RBTreeNode<T> node)
    {
        node.color=BLACK;
    }

    public void setRed(RBTreeNode<T> node)
    {
        node.color=RED;
    }

    /*
     * 红黑树插入修正函数
     *
     * 在向红黑树中插入节点之后(失去平衡)，再调用该函数；
     * 目的是将它重新塑造成一颗红黑树。
     *
     * 参数说明：
     *     node 插入的结点        // 对应《算法导论》中的z
     */
    private void insertFixUp(RBTreeNode<T> node) {
        RBTreeNode<T> parent, gparent;//父节点，祖父节点

        // 若“父节点存在，并且父节点的颜色是红色”
        while (((parent = parentOf(node))!=null) && isRed(parent)) {
            gparent = parentOf(parent);

            //若“父节点”是“祖父节点的左孩子”
            if (parent == gparent.left) {
                // Case 1条件：叔叔节点是红色
                RBTreeNode<T> uncle = gparent.right;
                if ((uncle!=null) && isRed(uncle)) {
                    setBlack(uncle);
                    setBlack(parent);
                    setRed(gparent);
                    node = gparent;
                    continue;
                }

                // Case 2条件：叔叔是黑色，且当前节点是右孩子
                if (parent.right == node) {
                    RBTreeNode<T> tmp;
                    leftRotate(parent);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }

                // Case 3条件：叔叔是黑色，且当前节点是左孩子。
                setBlack(parent);
                setRed(gparent);
                rightRotate(gparent);
            } else {    //若“z的父节点”是“z的祖父节点的右孩子”
                // Case 1条件：叔叔节点是红色
                RBTreeNode<T> uncle = gparent.left;
                if ((uncle!=null) && isRed(uncle)) {
                    setBlack(uncle);
                    setBlack(parent);
                    setRed(gparent);
                    node = gparent;
                    continue;
                }

                // Case 2条件：叔叔是黑色，且当前节点是左孩子
                if (parent.left == node) {
                    RBTreeNode<T> tmp;
                    rightRotate(parent);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }

                // Case 3条件：叔叔是黑色，且当前节点是右孩子。
                setBlack(parent);
                setRed(gparent);
                leftRotate(gparent);
            }
        }

        // 将根节点设为黑色
        setBlack(this.root);
    }
}
