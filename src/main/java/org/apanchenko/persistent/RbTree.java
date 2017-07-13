package org.apanchenko.persistent;

/**
 * Persistent red-black tree.<p>
 *
 * Features:
 * <ul>
 *  <li>RbTree requires O(N) space.</li>
 *  <li>{@code find}, {@code insert} and {@code remove} operations
 *  cost 0(log2(N)) time and space.</li>
 * </ul><p>
 *
 * Implementation details:
 * <ul>
 *  <li>RbTree does not define special node type, it is a node itself</li>
 *  <li>Recursion actively used, it permits to get rid of link to parent node</li>
 *  <li>The only 'special' node is head, denoted by null key</li>
 *  <li>While RbTree is persistent, fields are not final. Their values may change
 *      for new nodes while constructing new tree inside {@code insert}
 *      or {@code remove}. This allows to minimize the number of allocations.</li>
 *  <li>Since fields are not declared final, it appears very easy to break
 *      persistancy of {@code RbTree} instance. To make code more obvious added
 *      underscore prefix to variables that are subject to change inside {@code insert}
 *      or {@code remove}.</li>
 * </ul>
 *
 * @author Anton Panchenko
 * @version 0.1.2
 */
public class RbTree<T> {
    private static final int INVALID = -1; // used in validation
    private T key; // user data, is null for head only
    private boolean red; // head and root always black
    private RbTree<T> left; // root is always left to the head
    private RbTree<T> right;

    /** Creates an empty RbTree */
    public RbTree() {
        key = null; //
        red = false; // head is black
    }

    /** Creates a new node */
    private RbTree(T key) {
        this.key = key;
        red = true; // new leaf always red
    }

    /** Creates a new node */
    private RbTree(T key, boolean red, RbTree<T> left, RbTree<T> right) {
        this.key = key;
        this.red = red;
        this.left = left;
        this.right = right;
    }

    /**
     * Duplicates a node
     * @param from node to copy
     */
    private RbTree(RbTree<T> from) {
        this.key = from.key;
        this.red = from.red;
        this.left = from.left;
        this.right = from.right;
    }

    /**
     * Returns a hash code for this tree. The hash code for a
     * {@code RbTree} object is computed as a combinations of
     * hash codes all values mixed with node colors.
     * (The hash value of the empty tree is zero.)
     * Takes O(n) time.
     *
     * @return  a hash code value for this object.
     */
    @Override public int hashCode() {
        int hash = 0;
        if (key != null)
            hash = key.hashCode() * (red ? 1 : -1);
        if (left != null)
            hash += left.hashCode();
        if (right != null)
            hash += right.hashCode();
        return hash;
    }

    /**
     * @return true if tree is empty.
     */
    public boolean empty() {
        return head() && left == null; // root always left
    }

    /**
     * Calculates number of nodes in a tree.
     * Takes O(n) time.
     *
     * @return  a number of values stored in a {@code RbTree} object.
     */
    public int size() {
        if (empty())
            return 0;
        if (head())
            return left.size();
        return 1 +
                (left==null ? 0 : left.size()) +
                (right==null ? 0 : right.size());
    }

    /**
     * Returns a {@code String} object representing this
     * {@code RbTree}'s value. To be used for debug purposes.
     * Takes O(n) time.
     *
     * @return  a string representation of the tree.
     */
    public String toString() {
        if (empty())
            return "is empty";
        if (head())
            return "head" + left.toString();
        return "(" + key + (red ? "R" : "B")
                + (left==null ? "?" : left)
                + (right==null ? "?" : right) + ")";
    }

    /**
     * Validates this {@code RbTree} object for consistency with
     * red-black tree rules:
     * <ul>
     *     <li>The root is black.</li>
     *     <li>If a node is red, then both its children are black.</li>
     *     <li>Every path from a given node to any of its descendant
     *          leafs contains the same number of black nodes.</li>
     * </ul>
     * Takes O(n) time.
     *
     * @return  true if this tree is a valid red-black tree.
     */
    public boolean valid() {
        if (!head() || red)
            return false;
        return empty() || (!left.red && left.validBlackHeight() != INVALID);
    }

    /**
     * Returns a key found in a this tree or null.
     * Takes O(log2(n)) time.
     *
     * @param key is a key to find, cannot be null
     * @return  a found key or null
     */
    public T find(T key) {
        if (key == null)
            throw new NullPointerException();
        if (empty())
            return null;

        RbTree<T> n;
        if (head())
            n = left.findNode(key); // root always left
        else
            n = findNode(key);

        return n == null ? null : n.key;
    }

    /**
     * Adds a new key into this tree.
     * Takes O(log2(n)) time.
     *
     * @param key a key to find, cannot be null
     * @return found key or null
     */
    public RbTree<T> insert(T key) {
        if (key == null)
            throw new NullPointerException();

        RbTree<T> _this = this;
        if (empty()) { // create a root
            _this = new RbTree<>(this);
            _this.left = new RbTree<>(key); // root is always left
            _this.left.red = false; // root is always black
        }
        else { // insert to root
            RbTree<T> n = left.insertCopy(key);
            if (n != left) {
                _this = new RbTree<>(this); // clone head
                _this.left = n; // set new root
                _this.left.insertFix(_this); // fix root
                _this.left.red = false; // restore root blackness
            }
        }
        return _this;
    }

    /**
     * @return this if not changed or a new node.
     */
    private RbTree<T> insertCopy(T insKey) {
        if (insKey.equals(this.key)) // already has that key
            return this; // return self if not changed

        RbTree<T> _this = this; // return self if not changed
        if (isLeft(insKey)) { // insert to left
            if (left == null) {
                _this = new RbTree<>(this); // clone self
                _this.left = new RbTree<>(insKey); // spawn left child
            }
            else {
                RbTree<T> n = left.insertCopy(insKey);
                if (n != left) { // if left changed
                    _this = new RbTree<>(this); // clone self
                    _this.left = n;
                    _this.left.insertFix(_this); // fix left
                }

            }
        }
        else { // insert to right
            if (right == null) {
                _this = new RbTree<>(this);
                _this.right = new RbTree<>(insKey); // create right child
            }
            else {
                RbTree<T> n = right.insertCopy(insKey);
                if (n != right) { // right changed
                    _this = new RbTree<>(this); // clone self
                    _this.right = n;
                    _this.right.insertFix(_this); // fix right
                }
            }
        }
        return _this;
    }

    /**
     * this and parent are copies
     */
    private void insertFix(RbTree<T> parent) {
        // node, father, uncle red;
        if (isRed(left) && isRed(right)) {
            if (isRed(left.left) || isRed(left.right) || isRed(right.left) || isRed(right.right)) {
                red = true; // this is a copy
                left = new RbTree<>(left.key, false, left.left, left.right);
                right = new RbTree<>(right.key, false, right.left, right.right);
            }
        }

        // node, father red; uncle black
        else if (isRed(left)) {
            if (isRed(left.right)) // l.r is new node
                left.rotateLeft(this); // red left.right becomes left.left
            if (isRed(left.left)) {
                red = true; // this is a copy
                left = new RbTree<>(left.key, false, left.left, left.right);
                rotateRight(parent);
            }
        }

        // node, father red; uncle black
        else if (isRed(right)) {
            if (isRed(right.left)) // r.l is new node
                right.rotateRight(this); // red r.l becomes r.r
            if (isRed(right.right)) {
                red = true; // this is a copy
                right = new RbTree<>(right.key, false, right.left, right.right);
                rotateLeft(parent);
            }
        }
    }

    /**   p             p
     *    b             d
     *  a   d    ->   b   e
     *     c e       a c
     *
     * Change: p, b, d - are copies or new
     */
    private void rotateLeft(RbTree<T> parent) {
        RbTree<T> d = right;
        if (parent != null) {
            if (this == parent.left) // p-d
                parent.left = right;
            else
                parent.right = right;
        }
        right = d.left; // b-c
        d.left = this; // d-b
    }

    /**    p             p
     *     d             b
     *   b   e    ->   a   d
     *  a c               c e
     *
     * Change: p, d, b - are copies or new
     */
    private void rotateRight(RbTree<T> _parent) {
        RbTree<T> _b = left;
        if (_parent != null) {
            if (this == _parent.left) // p-b
                _parent.left = _b;
            else
                _parent.right = _b;
        }
        left = _b.right; // d-c
        _b.right = this; // b-d
    }

    /**
     * Helper.
     */
    private class Fixup {
        boolean required;
    }

    /**
     * Remove node by key.
     *
     * @param key object to be removed from the tree, if present
     * @return resulting tree without key or unchanged this
     *         if key is not present
     */
    public RbTree<T> remove(T key) {
        if (key == null)
            throw new NullPointerException();
        if (empty()) // is already empty
            return this; // return self

        Fixup fixup = new Fixup(); // output argument for fix up
        return removeOnSon(null, key, left, fixup);
    }

    /**
     * @return new parent if modified
     */
    private RbTree<T> removeOnSon(RbTree<T> parent, T remKey, RbTree<T> son, Fixup fixup) {
        if (son == null)
            return parent;

        int remHash = remKey.hashCode(); // first compare hashes
        int sonHash = son.key.hashCode();

        // descend left or right subtree
        RbTree<T> grandSon = null;
        if (remHash < sonHash)
            grandSon = son.left;
        else if (!remKey.equals(son.key))
            grandSon = son.right;

        if (grandSon != null) {
            boolean removeLeft = (left == son);
            RbTree<T> _this = son.removeOnSon(this, remKey, grandSon, fixup);
            if (parent == null)
                return _this; // new head
            if (_this == this)
                return parent; // nothing changed

            RbTree<T> _parent = new RbTree<>(parent);
            _parent.swapSon(this, _this);
            if (fixup.required)
                _this.removeFix(_parent, removeLeft, fixup);
            return _parent;
        }

        // remove son with both children
        RbTree<T> _this = new RbTree<>(this);

        RbTree<T> _parent = null;
        if (parent != null) {
            _parent = new RbTree<>(parent);
            _parent.swapSon(this, _this);
        }

        if (son.left != null && son.right != null) { // have both children
            RbTree<T> _son;
            if (son.left.right == null) { // just left
                _son = new RbTree<>(son.left.key, son.red, son.left.left, son.right);
                boolean remLeft = _this.swapSon(son, _son);
                _son.removeFix(_this, true, fixup);
                if (fixup.required)
                    _this.removeFix(_parent, remLeft, fixup);
                return _parent == null ? _this : _parent; // new head or new parent
            }
            else {
                RbTree<T> _sonLeft = new RbTree<>(son.left); // find rightmost on the left
                _son = new RbTree<>(null, son.red, _sonLeft, son.right); //
                _this.swapSon(son, _son);
                _sonLeft.removeRightmost(_son, _son, fixup); // get key from rightmost
                if (fixup.required)
                    _son.removeFix(_this,true, fixup);
                if (fixup.required)
                    _this.removeFix(_parent, son == left, fixup);
                return _parent == null ? _this : _parent; // new head or new parent
            }
        }

        // remove son with zero or one child
        _this.removeSon(_parent, son, fixup);

        return _parent == null ? _this : _parent; // new head or new parent
    }

    /**
     * Swap store key with rightmost key.
     *
     * @param _parent parent node of this node.
     * @param _store receive key of rightmost node
     * @param fixup control fixup for parent node
     */
    private void removeRightmost(RbTree<T> _parent, RbTree<T> _store, Fixup fixup) {
        assert(_parent != null);
        assert(_parent.left == this || _parent.right == this);
        assert(right != null);

        if (right.right == null) { // found rightmost
            _store.key = right.key;
            removeSon(_parent, right, fixup); // right may have left
        }
        else { // go deeper right
            right = new RbTree<>(right);
            right.removeRightmost(this, _store, fixup);
            if (fixup.required)
                removeFix(_parent, false, fixup);
        }
    }

    /**
     * Parent copy, this copy.
     */
    private void removeSon(RbTree<T> _parent, RbTree<T> son, Fixup fixup) {
        assert(_parent == null || _parent.left == this || _parent.right == this);
        assert(son != null);
        assert(son == left || son == right); // valid son
        assert(son.left == null || son.right == null); // son has zero or one child

        // remove node with no children
        if (son.left == null && son.right == null) {
            if (son.red) {
                left = right = null; // remove single red son
                fixup.required = false;
            }
            else { // black
                boolean removeLeft = (son == left);
                swapSon(son, null);
                removeFix(_parent, removeLeft, fixup);
            }
        }
        else { // remove node with one child
            assert(!son.red);
            RbTree<T> grandson = (son.left==null ? son.right : son.left);
            assert(grandson.left == null && grandson.right == null);
            assert(grandson.red);

            son.key = grandson.key;

            if (son.left == null)
                son.right = null;
            else
                son.left = null;

            fixup.required = false; // do not fix up
        }
    }

    /**
     * Parent copy, this copy.
     */
    private void removeFix(RbTree<T> _parent, boolean remLeft, Fixup fixup) {
        assert(_parent == null || _parent.left == this || _parent.right == this);

        fixup.required = false;

        RbTree<T> b = remLeft ? right : left; // brother of deleted node
        if (b == null)
            return;
        RbTree<T> _b = new RbTree<>(b);
        swapSon(b, _b);

        if (red) {
            RbTree<T> c = _b.getRedSon(); // red nephew if any
            if (c != null)
                removeCase1(_parent, remLeft, _b, c); // case 1: red parent, red nephew
            else
                removeCase2(_b); // case 2: red parent, no red nephew
        }
        else {
            if (_b.red) {
                RbTree<T> c = remLeft ? _b.left : _b.right;
                if (c == null)
                    c = remLeft ? _b.right : _b.left; // nephew

                if (c != null) {
                    RbTree<T> d = c.getRedSon();
                    if (d != null)
                        removeCase3(_parent, remLeft, _b, c, d); // case 3: red brother, red grandnephew
                    else
                        removeCase4(_parent, remLeft, _b, c); // case 4: red brother, no red grandnephew
                }
            }
            else {
                RbTree<T> c = _b.getRedSon(); // red nephew if any
                if (c != null)
                    removeCase5(_parent, remLeft, _b, c); // case 5: red nephew
                else {
                    removeCase6(_b); // case 5: black
                    fixup.required = true;
                }
            }
        }
    }

    /**
     * parent, this, b - copies
     */
    private void removeCase1(RbTree<T> _parent, boolean remLeft, RbTree<T> _b, RbTree<T> c) {
        RbTree<T> _c = new RbTree<>(c);
        _b.swapSon(c, _c); // copy nephew

        if (remLeft) {
            if (_b.left == _c)
                _b.rotateRight(this);
            else
                _c.red = !(_b.red = true);

            rotateLeft(_parent);
        }
        else {
            if (_b.right == _c)
                _b.rotateLeft(this);
            else
                _c.red = !(_b.red = true);

            rotateRight(_parent);
        }
        red = false;
    }


    /**
     * Call on copy.
     */
    private void removeCase2(RbTree<T> _b) {
        red = false;
        _b.red = true;
    }

    /**
     * Call on copy.
     */
    private void removeCase3(RbTree<T> _parent, boolean remLeft, RbTree<T> _b, RbTree<T> c, RbTree<T> d) {
        RbTree<T> _c = new RbTree<>(c);
        _b.swapSon(c, _c); // copy nephew

        RbTree<T> _d = new RbTree<>(d);
        _c.swapSon(d, _d); // copy nephew

        if (remLeft) {
            if (_b.right == _c) { // cl
                _b.red = false; // 2
                if (_c.right == _d) {
                    _c.rotateLeft(this);
                    _d.red = true; // 3
                    _c.red = false; // 4
                }
                else {
                    _c.red = true; // 3
                    _d.red = false; // 4
                }
            }
            else { // cr
                _b.red = true; // 3
                if (_c.left == _d) {// dr
                    _c.rotateRight(_b);
                    _c.red = false; // 4
                    _d.red = false; // 2
                }
                else {
                    _c.red = false; // 2
                    _d.red = false; // 4
                }
                _b.rotateRight(this);
            }
            rotateLeft(_parent);
        }
        else {
            if (_b.left == _c) { // cl
                _b.red = false; // 2
                if (_c.left == _d) {
                    _c.rotateRight(_b);
                    _d.red = true; // 3
                    _c.red = false; // 4
                }
                else {
                    _c.red = true; // 3
                    _d.red = false; // 4
                }
            }
            else { // cr
                _b.red = true; // 3
                if (_c.right == _d) {// dr
                    _c.rotateLeft(_b);
                    _c.red = false; // 4
                    _d.red = false; // 2
                }
                else {
                    _c.red = false; // 2
                    _d.red = false; // 4
                }
                _b.rotateLeft(this);
            }
            rotateRight(_parent);
        }
    }

    /**
     * Call on copy.
     */
    private void removeCase4(RbTree<T> _parent, boolean remLeft, RbTree<T> _b, RbTree<T> c) {
        RbTree<T> _c = new RbTree<>(c);
        _b.swapSon(c, _c); // copy nephew

        if (remLeft) {
            if (_b.right == _c)
                _b.rotateLeft(this);
            else
                _c.red = !(_b.red = false);
            rotateLeft(_parent);
        }
        else {
            if (_b.left == _c)
                _b.rotateRight(this);
            else
                _c.red = !(_b.red = false);
            rotateRight(_parent);
        }
    }

    /**
     * Call on copy.
     */
    private void removeCase5(RbTree<T> _parent, boolean remLeft, RbTree<T> _b, RbTree<T> c) {
        RbTree<T> _c = new RbTree<>(c);
        _c.red = false;
        _b.swapSon(c, _c); // copy nephew

        if (remLeft) {
            if (_b.left == _c)
                _b.rotateRight(this);
            rotateLeft(_parent);
        }
        else {
            if (_b.right == _c)
                _b.rotateLeft(this);
            rotateRight(_parent);
        }
    }

    /**
     * Call on copy.
     */
    private void removeCase6(RbTree<T> _b) {
        _b.red = true;
    }

    private boolean head() {
        return key == null;
    }

    private boolean isRed(RbTree<T> n) {
        return n != null && n.red;
    }

    private boolean isLeft(T a) {
        return a.hashCode() < key.hashCode();
    }

    /**
     * @return red son
     */
    private RbTree<T> getRedSon() {
        if (isRed(left))
            return left;
        if (isRed(right))
            return right;
        return null;
    }

    /**
     * @return true if left changed
     */
    private boolean swapSon(RbTree<T> son, RbTree<T> newSon) {
        assert(left == son || right == son);
        if (left == son) {
            left = newSon;
            return true;
        }
        right = newSon;
        return false;
    }

    /**
     * @return node with equal key
     */
    private RbTree<T> findNode(T findKey) {
        int findHash = findKey.hashCode();
        int myHash = this.key.hashCode(); // first compare hashes
        if (findHash < myHash) // search left
            return left == null ? null : left.findNode(findKey);
        if (findHash > myHash || !this.key.equals(findKey)) // search right
            return right == null ? null : right.findNode(findKey);
        return this; // key equals findData
    }

    /**
     * Validate subtree.
     * @return number of black nodes in one path or INVALID if subtree invalid
     */
    private int validBlackHeight() {
        if (isRed(this) && (isRed(left) || isRed(right)))
            return INVALID; // red violation

        int leftBh = 0;
        if (left != null) {
            if (left.isLeft(key))
                return INVALID; // wrong key order
            leftBh = left.validBlackHeight();
            if (leftBh == INVALID)
                return INVALID;
        }

        int rightBh = 0;
        if (right != null) {
            if (isLeft(right.key))
                return INVALID; // wrong key order
            rightBh = right.validBlackHeight();
            if (rightBh == INVALID)
                return INVALID;
        }

        if (leftBh != rightBh)
            return INVALID;

        return leftBh + (red ? 0 : 1);
    }
}