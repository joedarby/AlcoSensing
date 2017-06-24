package com.joedarby.alcosensing1.Sensing;

import java.util.LinkedList;

public class BoundedLinkedList<E> extends LinkedList<E> {

    private final int capacity;

    public BoundedLinkedList(int capacity) {
        super();
        this.capacity = capacity;
    }

    @Override
    public boolean add(E object) {
        if (super.size() >= capacity) {
            super.removeFirst();
            super.add(object);
        } else {
            super.add(object);
        }
        return true;
    }

}
