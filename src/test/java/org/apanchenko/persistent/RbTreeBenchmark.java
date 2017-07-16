package org.apanchenko.persistent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class RbTreeBenchmark {

    private ArrayList<Integer> ints;
    private RbTree<Integer> rbTree;
    private org.pcollections.OrderedPSet<Integer> pcol;
    private org.organicdesign.fp.collections.PersistentTreeSet<Integer> paguro;
    private com.github.andrewoma.dexx.collection.TreeSet<Integer> dexx;
    private io.vavr.collection.TreeSet<Integer> vavr;

    @Param({"10", "100", "1000", "10000"})
    int size;

    @Setup
    public void setup() throws Exception {
        ints = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            ints.add(i);
        Collections.shuffle(ints, new Random(2017));

        pcol = org.pcollections.OrderedPSet.from(ints);
        paguro = org.organicdesign.fp.collections.PersistentTreeSet.of(ints);
        dexx = com.github.andrewoma.dexx.collection.TreeSet.empty();
        rbTree = RbTree.empty();
        vavr = io.vavr.collection.TreeSet.<Integer>empty().addAll(ints);
        for (Integer x : ints) {
            rbTree = rbTree.insert(x);
            dexx = dexx.add(x);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Dexx
    @Benchmark public com.github.andrewoma.dexx.collection.TreeSet<Integer> dexx_empty() {
        return com.github.andrewoma.dexx.collection.TreeSet.empty();
    }
    @Benchmark public boolean dexx_contains() {
        return dexx.contains(size / 2);
    }
    @Benchmark public com.github.andrewoma.dexx.collection.TreeSet<Integer> dexx_insert() {
        return dexx.add(size / 2);
    }
    @Benchmark public com.github.andrewoma.dexx.collection.TreeSet<Integer> dexx_remove() {
        return dexx.remove(size / 2);
    }
    @Benchmark public int dexx_hashCode() {
        return dexx.hashCode();
    }

    ///////////////////////////////////////////////////////////////////////////
    // RbTree
    @Benchmark public RbTree<Integer> rbTree_empty() {
        return RbTree.empty();
    }
    @Benchmark public boolean rbTree_contains() {
        return rbTree.contains(size / 2);
    }
    @Benchmark public RbTree<Integer> rbTree_insert() {
        return rbTree.insert(size / 2);
    }
    @Benchmark public RbTree<Integer> rbTree_remove() {
        return rbTree.remove(size / 2);
    }
    @Benchmark public int rbTree_hashCode() {
        return rbTree.hashCode();
    }

    ///////////////////////////////////////////////////////////////////////////
    // PCollections
    @Benchmark public org.pcollections.OrderedPSet<Integer> pcollections_empty() {
        return org.pcollections.OrderedPSet.empty();
    }
    @Benchmark public boolean pcollections_contains() {
        return pcol.contains(size / 2);
    }
    @Benchmark public org.pcollections.OrderedPSet<Integer> pcollections_insert() {
        return pcol.plus(size / 2);
    }
    @Benchmark public org.pcollections.OrderedPSet<Integer> pcollections_remove() {
        return pcol.minus(size / 2);
    }
    @Benchmark public int pcollections_hashCode() {
        return pcol.hashCode();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Paguro
    @Benchmark public org.organicdesign.fp.collections.PersistentTreeSet<Integer> paguro_empty() {
        return org.organicdesign.fp.collections.PersistentTreeSet.empty();
    }
    @Benchmark public boolean paguro_contains() {
        return paguro.contains(size / 2);
    }
    @Benchmark public org.organicdesign.fp.collections.PersistentTreeSet<Integer> paguro_insert() {
        return paguro.put(size / 2);
    }
    @Benchmark public org.organicdesign.fp.collections.PersistentTreeSet<Integer> paguro_remove() {
        return paguro.without(size / 2);
    }
    @Benchmark public int paguro_hashCode() {
        return paguro.hashCode();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Vavr
    @Benchmark public io.vavr.collection.TreeSet<Integer> vavr_empty() {
        return io.vavr.collection.TreeSet.empty();
    }
    @Benchmark public boolean vavr_contains() {
        return vavr.contains(size / 2);
    }
    @Benchmark public io.vavr.collection.TreeSet<Integer> vavr_insert() {
        return vavr.add(size / 2);
    }
    @Benchmark public io.vavr.collection.TreeSet<Integer> vavr_remove() {
        return vavr.remove(size / 2);
    }
    @Benchmark public int vavr_hashCode() {
        return vavr.hashCode();
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RbTreeBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}