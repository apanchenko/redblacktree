package org.apanchenko.persistent;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.infra.Blackhole;

import org.pcollections.OrderedPSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class RbTreeBenchmark {
    private ArrayList<Integer> data;
    private RbTree<Integer> rbTree;
    private OrderedPSet<Integer> set;

    @Param({"10", "1000"})
    int size;

    @Setup
    public void setup() {
        data = new ArrayList<>(size);
        for (int i = 0; i < data.size(); i++)
            data.add(i);
        Collections.shuffle(data, new Random(2017));

        rbTree = RbTree.empty();
        set = OrderedPSet.empty();
        for (Integer x : data) {
            rbTree = rbTree.insert(x);
            set = set.plus(x);
        }
    }

    @Benchmark
    @Group("RbTree")
    public RbTree<Integer> emptyRbTree() {
        return RbTree.empty();
    }

    @Benchmark
    @Group("RbTree")
    public RbTree<Integer> insertRbTree() {
        RbTree<Integer> tree = RbTree.empty();
        for (Integer x : data)
            tree = tree.insert(x);
        return tree;
    }

    @Benchmark
    @Group("RbTree")
    public void removeRbTree(Blackhole bh) {
        for (Integer x : data)
            bh.consume(rbTree.remove(x));
    }

    @Benchmark
    @Group("PCollections")
    public OrderedPSet<Integer> emptyPCollection() {
        return OrderedPSet.empty();
    }

    @Benchmark
    @Group("PCollections")
    public OrderedPSet<Integer> insertPCollection() {
        OrderedPSet<Integer> set = OrderedPSet.empty();
        for (Integer x : data)
            set = set.plus(x);
        return set;
    }

    @Benchmark
    @Group("PCollections")
    public void removePCollection(Blackhole bh) {
        for (Integer x : data)
            bh.consume(set.minus(x));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RbTreeBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}