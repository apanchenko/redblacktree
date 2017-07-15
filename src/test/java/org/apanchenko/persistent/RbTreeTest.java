package org.apanchenko.persistent;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RbTreeTest {

    @Test
    void incorrectInput() {
        RbTree<Boolean> rbt = RbTree.empty();
        assertThrows(NullPointerException.class, () -> rbt.insert(null) );
        assertThrows(NullPointerException.class, () -> rbt.remove(null) );
        assertThrows(NullPointerException.class, () -> rbt.find(null) );
        assertTrue(rbt.valid());
        assertTrue(rbt.isEmpty());
    }

    @Test
    void emptiness() {
        RbTree<String> rbt = RbTree.empty();
        assertTrue(rbt.isEmpty());
        assertNull(rbt.find("any"));
        assertEquals(0, rbt.size());
    }

    @Test
    void basicInsert() {
        RbTree<String> t1 = RbTree.empty().insert("Hello");
        RbTree<String> t2 = t1.insert("brown").insert("fox");
        RbTree<String> t3 = t2.insert("!!!");

        // t1 = {Hello}
        assertEquals(1, t1.size());
        assertTrue(t1.valid());

        // t2 = {Hello brown fox}
        assertEquals(3, t2.size());
        assertTrue(t2.valid());

        // t3 = {Hello brown fox !!!}
        assertEquals(4, t3.size());
        assertTrue(t3.valid());
        assertNull(t3.find("red"));
        assertEquals("fox", t3.find("fox"));
    }

    @Test
    void insertPattern() {
        RbTree<Integer> t;
        t = validInsert(RbTree.empty(), new int[]{5, 1, 4, 3, 2});
        t = validInsert(RbTree.empty(), new int[]{5, 1, 4, 3, 2});
    }

    @Test
    void persistence() {
        RbTree<Integer> tree = RbTree.empty();
        Random rand = new Random();

        // compare tree hashes before and after operations
        for (int i = 0; i < 1000; i++) {
            int hash = tree.hashCode();
            RbTree<Integer> t = tree;
            t = validInsert(t, rand.nextInt(500));
            t = validRemove(t, rand.nextInt(500));
            assertEquals(hash, tree.hashCode());
            tree = t;
        }
    }

    @Test
    void insertAndDelete() {
        // test insert-delete with shuffled data
        for(int count = 1; count < 100; ++count) {
            // generate test data
            ArrayList<Integer> data = new ArrayList<>(count);
            for (int i = 0; i < count; i++)
                data.add(i);

            for (int seed = 0; seed < 1000; ++seed) {

                RbTree<Integer> tree = RbTree.empty();

                // insert data in random order
                Random random = new Random(seed);
                Collections.shuffle(data, random);
                for (Integer x : data)
                    tree = validInsert(tree, x);

                // remove all data in another order
                Collections.shuffle(data, random);
                for (Integer x : data)
                    tree = validRemove(tree, x);

                assertTrue(tree.isEmpty());
            }
        }
    }

    private RbTree<Integer> validInsert(RbTree<Integer> tree, int[] keys) {
        for (int key:keys) {
            tree = tree.insert(key);
            assertTrue(tree.valid());
        }
        return tree;
    }

    private RbTree<Integer> validInsert(RbTree<Integer> tree, int key) {
        tree = tree.insert(key);
        assertTrue(tree.valid());
        return tree;
    }

    private RbTree<Integer> validRemove(RbTree<Integer> tree, int key) {
        tree = tree.remove(key);
        assertTrue(tree.valid());
        return tree;
    }
}