package com.kerns.structure.tree;

/**
 * 红黑数的实现
 * 创建人:kerns
 * 创建时间: 2019/6/19 2:01 PM
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
    }
    /*
     * 对红黑树的节点(x)进行左旋转
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
    private void leftRotate(RBTreeNode<T> node)
    {
        RBTreeNode right= node.right; //先把右节点拿到
        node.right=right.left;//把该节点的右节点换成
        node.parent=node.right;
        node.left=node.parent;
    }
    /*
     * 对红黑树的节点(y)进行右旋转
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
    private void rightRotate(RBTreeNode<T> node)
    {

    }
}
