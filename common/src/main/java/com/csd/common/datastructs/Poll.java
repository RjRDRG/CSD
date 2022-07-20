package com.csd.common.datastructs;

import java.util.*;
import java.util.function.Predicate;

public class Poll<T> {

    private final Queue<T> queue;

    public Poll(Comparator<T> comparator) {
        this.queue = new PriorityQueue<>(comparator);
    }

    public Poll() {
        this.queue = new LinkedList<>();
    }

    public synchronized void add(T t) {
        queue.add(t);
    }

    public synchronized void remove(T t) {
        queue.removeIf(tr -> tr.equals(t));
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized List<T> getN(int n) {
        List<T> t = new ArrayList<>(n);
        Iterator<T> it = queue.iterator();
        for(int i=0; i<n; i++) {
            if(it.hasNext())
                t.add(it.next());
            else
                return t;
        }
        return t;
    }

    public synchronized T getElement(Predicate<T> predicate) {
        return queue.stream().filter(predicate).findAny().orElse(null);
    }

    public synchronized T poll() {
        return queue.poll();
    }
}
