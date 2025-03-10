package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

public class ArrayListDeque<T> extends AbstractList<T> implements ListAndDeque<T> {

    private static final int MIN_GROWTH = 1;
    private Object[] contents;
    private int head;
    private int size;

    public ArrayListDeque() {
        this(1);
    }

    public ArrayListDeque(int i) {
        this.contents = new Object[i];
        this.head = 0;
        this.size = 0;
    }

    public int size() {
        return this.size;
    }

    @VisibleForTesting
    public int capacity() {
        return this.contents.length;
    }

    private int getIndex(int i) {
        return (i + this.head) % this.contents.length;
    }

    public T get(int i) {
        this.verifyIndexInRange(i);
        return this.getInner(this.getIndex(i));
    }

    private static void verifyIndexInRange(int i, int j) {
        if (i < 0 || i >= j) {
            throw new IndexOutOfBoundsException(i);
        }
    }

    private void verifyIndexInRange(int i) {
        verifyIndexInRange(i, this.size);
    }

    private T getInner(int i) {
        return this.contents[i];
    }

    public T set(int i, T t0) {
        this.verifyIndexInRange(i);
        Objects.requireNonNull(t0);
        int j = this.getIndex(i);
        T t1 = this.getInner(j);

        this.contents[j] = t0;
        return t1;
    }

    public void add(int i, T t0) {
        verifyIndexInRange(i, this.size + 1);
        Objects.requireNonNull(t0);
        if (this.size == this.contents.length) {
            this.grow();
        }

        int j = this.getIndex(i);

        if (i == this.size) {
            this.contents[j] = t0;
        } else if (i == 0) {
            --this.head;
            if (this.head < 0) {
                this.head += this.contents.length;
            }

            this.contents[this.getIndex(0)] = t0;
        } else {
            for (int k = this.size - 1; k >= i; --k) {
                this.contents[this.getIndex(k + 1)] = this.contents[this.getIndex(k)];
            }

            this.contents[j] = t0;
        }

        ++this.modCount;
        ++this.size;
    }

    private void grow() {
        int i = this.contents.length + Math.max(this.contents.length >> 1, 1);
        Object[] aobject = new Object[i];

        this.copyCount(aobject, this.size);
        this.head = 0;
        this.contents = aobject;
    }

    public T remove(int i) {
        this.verifyIndexInRange(i);
        int j = this.getIndex(i);
        T t0 = this.getInner(j);

        if (i == 0) {
            this.contents[j] = null;
            ++this.head;
        } else if (i == this.size - 1) {
            this.contents[j] = null;
        } else {
            for (int k = i + 1; k < this.size; ++k) {
                this.contents[this.getIndex(k - 1)] = this.get(k);
            }

            this.contents[this.getIndex(this.size - 1)] = null;
        }

        ++this.modCount;
        --this.size;
        return t0;
    }

    public boolean removeIf(Predicate<? super T> predicate) {
        int i = 0;

        for (int j = 0; j < this.size; ++j) {
            T t0 = this.get(j);

            if (predicate.test(t0)) {
                ++i;
            } else if (i != 0) {
                this.contents[this.getIndex(j - i)] = t0;
                this.contents[this.getIndex(j)] = null;
            }
        }

        this.modCount += i;
        this.size -= i;
        return i != 0;
    }

    private void copyCount(Object[] aobject, int i) {
        for (int j = 0; j < i; ++j) {
            aobject[j] = this.get(j);
        }

    }

    public void replaceAll(UnaryOperator<T> unaryoperator) {
        for (int i = 0; i < this.size; ++i) {
            int j = this.getIndex(i);

            this.contents[j] = Objects.requireNonNull(unaryoperator.apply(this.getInner(i)));
        }

    }

    public void forEach(Consumer<? super T> consumer) {
        for (int i = 0; i < this.size; ++i) {
            consumer.accept(this.get(i));
        }

    }

    @Override
    public void addFirst(T t0) {
        this.add(0, t0);
    }

    @Override
    public void addLast(T t0) {
        this.add(this.size, t0);
    }

    public boolean offerFirst(T t0) {
        this.addFirst(t0);
        return true;
    }

    public boolean offerLast(T t0) {
        this.addLast(t0);
        return true;
    }

    @Override
    public T removeFirst() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        } else {
            return this.remove(0);
        }
    }

    @Override
    public T removeLast() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        } else {
            return this.remove(this.size - 1);
        }
    }

    @Override
    public ListAndDeque<T> reversed() {
        return new ArrayListDeque.b(this);
    }

    @Nullable
    public T pollFirst() {
        return this.size == 0 ? null : this.removeFirst();
    }

    @Nullable
    public T pollLast() {
        return this.size == 0 ? null : this.removeLast();
    }

    @Override
    public T getFirst() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        } else {
            return this.get(0);
        }
    }

    @Override
    public T getLast() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        } else {
            return this.get(this.size - 1);
        }
    }

    @Nullable
    public T peekFirst() {
        return this.size == 0 ? null : this.getFirst();
    }

    @Nullable
    public T peekLast() {
        return this.size == 0 ? null : this.getLast();
    }

    public boolean removeFirstOccurrence(Object object) {
        for (int i = 0; i < this.size; ++i) {
            T t0 = this.get(i);

            if (Objects.equals(object, t0)) {
                this.remove(i);
                return true;
            }
        }

        return false;
    }

    public boolean removeLastOccurrence(Object object) {
        for (int i = this.size - 1; i >= 0; --i) {
            T t0 = this.get(i);

            if (Objects.equals(object, t0)) {
                this.remove(i);
                return true;
            }
        }

        return false;
    }

    public Iterator<T> descendingIterator() {
        return new ArrayListDeque.a();
    }

    private class b extends AbstractList<T> implements ListAndDeque<T> {

        private final ArrayListDeque<T> source;

        public b(final ArrayListDeque arraylistdeque) {
            this.source = arraylistdeque;
        }

        @Override
        public ListAndDeque<T> reversed() {
            return this.source;
        }

        @Override
        public T getFirst() {
            return this.source.getLast();
        }

        @Override
        public T getLast() {
            return this.source.getFirst();
        }

        @Override
        public void addFirst(T t0) {
            this.source.addLast(t0);
        }

        @Override
        public void addLast(T t0) {
            this.source.addFirst(t0);
        }

        public boolean offerFirst(T t0) {
            return this.source.offerLast(t0);
        }

        public boolean offerLast(T t0) {
            return this.source.offerFirst(t0);
        }

        public T pollFirst() {
            return this.source.pollLast();
        }

        public T pollLast() {
            return this.source.pollFirst();
        }

        public T peekFirst() {
            return this.source.peekLast();
        }

        public T peekLast() {
            return this.source.peekFirst();
        }

        @Override
        public T removeFirst() {
            return this.source.removeLast();
        }

        @Override
        public T removeLast() {
            return this.source.removeFirst();
        }

        public boolean removeFirstOccurrence(Object object) {
            return this.source.removeLastOccurrence(object);
        }

        public boolean removeLastOccurrence(Object object) {
            return this.source.removeFirstOccurrence(object);
        }

        public Iterator<T> descendingIterator() {
            return this.source.iterator();
        }

        public int size() {
            return this.source.size();
        }

        public boolean isEmpty() {
            return this.source.isEmpty();
        }

        public boolean contains(Object object) {
            return this.source.contains(object);
        }

        public T get(int i) {
            return this.source.get(this.reverseIndex(i));
        }

        public T set(int i, T t0) {
            return this.source.set(this.reverseIndex(i), t0);
        }

        public void add(int i, T t0) {
            this.source.add(this.reverseIndex(i) + 1, t0);
        }

        public T remove(int i) {
            return this.source.remove(this.reverseIndex(i));
        }

        public int indexOf(Object object) {
            return this.reverseIndex(this.source.lastIndexOf(object));
        }

        public int lastIndexOf(Object object) {
            return this.reverseIndex(this.source.indexOf(object));
        }

        public List<T> subList(int i, int j) {
            return this.source.subList(this.reverseIndex(j) + 1, this.reverseIndex(i) + 1).reversed();
        }

        public Iterator<T> iterator() {
            return this.source.descendingIterator();
        }

        public void clear() {
            this.source.clear();
        }

        private int reverseIndex(int i) {
            return i == -1 ? -1 : this.source.size() - 1 - i;
        }
    }

    private class a implements Iterator<T> {

        private int index = ArrayListDeque.this.size() - 1;

        public a() {}

        public boolean hasNext() {
            return this.index >= 0;
        }

        public T next() {
            return ArrayListDeque.this.get(this.index--);
        }

        public void remove() {
            ArrayListDeque.this.remove(this.index + 1);
        }
    }
}
