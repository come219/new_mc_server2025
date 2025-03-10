package net.minecraft.util;

import java.io.Serializable;
import java.util.Deque;
import java.util.List;
import java.util.RandomAccess;
import javax.annotation.Nullable;

public interface ListAndDeque<T> extends Serializable, Cloneable, Deque<T>, List<T>, RandomAccess {

    ListAndDeque<T> reversed();

    T getFirst();

    T getLast();

    void addFirst(T t0);

    void addLast(T t0);

    T removeFirst();

    T removeLast();

    default boolean offer(T t0) {
        return this.offerLast(t0);
    }

    default T remove() {
        return this.removeFirst();
    }

    @Nullable
    default T poll() {
        return this.pollFirst();
    }

    default T element() {
        return this.getFirst();
    }

    @Nullable
    default T peek() {
        return this.peekFirst();
    }

    default void push(T t0) {
        this.addFirst(t0);
    }

    default T pop() {
        return this.removeFirst();
    }
}
