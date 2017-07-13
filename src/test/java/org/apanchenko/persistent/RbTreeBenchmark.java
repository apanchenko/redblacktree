package org.apanchenko.persistent;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.pcollections.OrderedPSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

@State(Scope.Benchmark)
public class RbTreeBenchmark
{
    private ArrayList<Integer> data;

    public RbTreeBenchmark() {
        data = new ArrayList<>(100);
        for (int i = 0; i < data.size(); i++)
            data.add(i);
        Collections.shuffle(data, new Random());
    }

    @Benchmark
    public int timeRbTree() {
        RbTree<Integer> tree = new RbTree<>();
        for (Integer x : data)
            tree = tree.insert(x);
        for (Integer x : data)
            tree = tree.remove(x);
        return tree.size();
    }

    @Benchmark
    public int timePCollection() {
        OrderedPSet<Integer> set = OrderedPSet.empty();
        for (Integer x : data)
            set = set.plus(x);
        for (Integer x : data)
            set = set.minus(x);
        return set.size();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RbTreeBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}