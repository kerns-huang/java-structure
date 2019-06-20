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
    private void rightRotate(RBTreeNode<T> y)
    {
        // 设置x是当前节点的左孩子。
        RBTreeNode<T> x = y.left;
    }
}
