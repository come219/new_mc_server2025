package net.minecraft.world.entity.player;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

public class AutoRecipeStackManager<T> {

    public final Reference2IntOpenHashMap<T> amounts = new Reference2IntOpenHashMap();

    public AutoRecipeStackManager() {}

    boolean hasAtLeast(T t0, int i) {
        return this.amounts.getInt(t0) >= i;
    }

    void take(T t0, int i) {
        int j = this.amounts.addTo(t0, -i);

        if (j < i) {
            throw new IllegalStateException("Took " + i + " items, but only had " + j);
        }
    }

    void put(T t0, int i) {
        this.amounts.addTo(t0, i);
    }

    public boolean tryPick(List<? extends AutoRecipeStackManager.a<T>> list, int i, @Nullable AutoRecipeStackManager.b<T> autorecipestackmanager_b) {
        return (new AutoRecipeStackManager.c(list)).tryPick(i, autorecipestackmanager_b);
    }

    public int tryPickAll(List<? extends AutoRecipeStackManager.a<T>> list, int i, @Nullable AutoRecipeStackManager.b<T> autorecipestackmanager_b) {
        return (new AutoRecipeStackManager.c(list)).tryPickAll(i, autorecipestackmanager_b);
    }

    public void clear() {
        this.amounts.clear();
    }

    public void account(T t0, int i) {
        this.put(t0, i);
    }

    List<T> getUniqueAvailableIngredientItems(Iterable<? extends AutoRecipeStackManager.a<T>> iterable) {
        List<T> list = new ArrayList();
        ObjectIterator objectiterator = Reference2IntMaps.fastIterable(this.amounts).iterator();

        while (objectiterator.hasNext()) {
            Entry<T> entry = (Entry) objectiterator.next();

            if (entry.getIntValue() > 0 && anyIngredientMatches(iterable, entry.getKey())) {
                list.add(entry.getKey());
            }
        }

        return list;
    }

    private static <T> boolean anyIngredientMatches(Iterable<? extends AutoRecipeStackManager.a<T>> iterable, T t0) {
        Iterator iterator = iterable.iterator();

        AutoRecipeStackManager.a autorecipestackmanager_a;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            autorecipestackmanager_a = (AutoRecipeStackManager.a) iterator.next();
        } while (!autorecipestackmanager_a.acceptsItem(t0));

        return true;
    }

    @VisibleForTesting
    public int getResultUpperBound(List<? extends AutoRecipeStackManager.a<T>> list) {
        int i = Integer.MAX_VALUE;
        ObjectIterable<Entry<T>> objectiterable = Reference2IntMaps.fastIterable(this.amounts);
        Iterator iterator = list.iterator();

        label31:
        while (iterator.hasNext()) {
            AutoRecipeStackManager.a<T> autorecipestackmanager_a = (AutoRecipeStackManager.a) iterator.next();
            int j = 0;
            ObjectIterator objectiterator = objectiterable.iterator();

            while (objectiterator.hasNext()) {
                Entry<T> entry = (Entry) objectiterator.next();
                int k = entry.getIntValue();

                if (k > j) {
                    if (autorecipestackmanager_a.acceptsItem(entry.getKey())) {
                        j = k;
                    }

                    if (j >= i) {
                        continue label31;
                    }
                }
            }

            i = j;
            if (j == 0) {
                break;
            }
        }

        return i;
    }

    private class c {

        private final List<? extends AutoRecipeStackManager.a<T>> ingredients;
        private final int ingredientCount;
        private final List<T> items;
        private final int itemCount;
        private final BitSet data;
        private final IntList path = new IntArrayList();

        public c(final List list) {
            this.ingredients = list;
            this.ingredientCount = list.size();
            this.items = AutoRecipeStackManager.this.getUniqueAvailableIngredientItems(list);
            this.itemCount = this.items.size();
            this.data = new BitSet(this.visitedIngredientCount() + this.visitedItemCount() + this.satisfiedCount() + this.connectionCount() + this.residualCount());
            this.setInitialConnections();
        }

        private void setInitialConnections() {
            for (int i = 0; i < this.ingredientCount; ++i) {
                AutoRecipeStackManager.a<T> autorecipestackmanager_a = (AutoRecipeStackManager.a) this.ingredients.get(i);

                for (int j = 0; j < this.itemCount; ++j) {
                    if (autorecipestackmanager_a.acceptsItem(this.items.get(j))) {
                        this.setConnection(j, i);
                    }
                }
            }

        }

        public boolean tryPick(int i, @Nullable AutoRecipeStackManager.b<T> autorecipestackmanager_b) {
            if (i <= 0) {
                return true;
            } else {
                int j = 0;

                while (true) {
                    IntList intlist = this.tryAssigningNewItem(i);
                    int k;
                    int l;

                    if (intlist == null) {
                        boolean flag = j == this.ingredientCount;
                        boolean flag1 = flag && autorecipestackmanager_b != null;

                        this.clearAllVisited();
                        this.clearSatisfied();
                        k = 0;

                        while (k < this.ingredientCount) {
                            l = 0;

                            while (true) {
                                if (l < this.itemCount) {
                                    if (!this.isAssigned(l, k)) {
                                        ++l;
                                        continue;
                                    }

                                    this.unassign(l, k);
                                    AutoRecipeStackManager.this.put(this.items.get(l), i);
                                    if (flag1) {
                                        autorecipestackmanager_b.accept(this.items.get(l));
                                    }
                                }

                                ++k;
                                break;
                            }
                        }

                        assert this.data.get(this.residualOffset(), this.residualOffset() + this.residualCount()).isEmpty();

                        return flag;
                    }

                    int i1 = intlist.getInt(0);

                    AutoRecipeStackManager.this.take(this.items.get(i1), i);
                    k = intlist.size() - 1;
                    this.setSatisfied(intlist.getInt(k));
                    ++j;

                    for (l = 0; l < intlist.size() - 1; ++l) {
                        int j1;
                        int k1;

                        if (isPathIndexItem(l)) {
                            j1 = intlist.getInt(l);
                            k1 = intlist.getInt(l + 1);
                            this.assign(j1, k1);
                        } else {
                            j1 = intlist.getInt(l + 1);
                            k1 = intlist.getInt(l);
                            this.unassign(j1, k1);
                        }
                    }
                }
            }
        }

        private static boolean isPathIndexItem(int i) {
            return (i & 1) == 0;
        }

        @Nullable
        private IntList tryAssigningNewItem(int i) {
            this.clearAllVisited();

            for (int j = 0; j < this.itemCount; ++j) {
                if (AutoRecipeStackManager.this.hasAtLeast(this.items.get(j), i)) {
                    IntList intlist = this.findNewItemAssignmentPath(j);

                    if (intlist != null) {
                        return intlist;
                    }
                }
            }

            return null;
        }

        @Nullable
        private IntList findNewItemAssignmentPath(int i) {
            this.path.clear();
            this.visitItem(i);
            this.path.add(i);

            while (!this.path.isEmpty()) {
                int j = this.path.size();
                int k;
                int l;

                if (isPathIndexItem(j - 1)) {
                    k = this.path.getInt(j - 1);

                    for (l = 0; l < this.ingredientCount; ++l) {
                        if (!this.hasVisitedIngredient(l) && this.hasConnection(k, l) && !this.isAssigned(k, l)) {
                            this.visitIngredient(l);
                            this.path.add(l);
                            break;
                        }
                    }
                } else {
                    k = this.path.getInt(j - 1);
                    if (!this.isSatisfied(k)) {
                        return this.path;
                    }

                    for (l = 0; l < this.itemCount; ++l) {
                        if (!this.hasVisitedItem(l) && this.isAssigned(l, k)) {
                            assert this.hasConnection(l, k);

                            this.visitItem(l);
                            this.path.add(l);
                            break;
                        }
                    }
                }

                k = this.path.size();
                if (k == j) {
                    this.path.removeInt(k - 1);
                }
            }

            return null;
        }

        private int visitedIngredientOffset() {
            return 0;
        }

        private int visitedIngredientCount() {
            return this.ingredientCount;
        }

        private int visitedItemOffset() {
            return this.visitedIngredientOffset() + this.visitedIngredientCount();
        }

        private int visitedItemCount() {
            return this.itemCount;
        }

        private int satisfiedOffset() {
            return this.visitedItemOffset() + this.visitedItemCount();
        }

        private int satisfiedCount() {
            return this.ingredientCount;
        }

        private int connectionOffset() {
            return this.satisfiedOffset() + this.satisfiedCount();
        }

        private int connectionCount() {
            return this.ingredientCount * this.itemCount;
        }

        private int residualOffset() {
            return this.connectionOffset() + this.connectionCount();
        }

        private int residualCount() {
            return this.ingredientCount * this.itemCount;
        }

        private boolean isSatisfied(int i) {
            return this.data.get(this.getSatisfiedIndex(i));
        }

        private void setSatisfied(int i) {
            this.data.set(this.getSatisfiedIndex(i));
        }

        private int getSatisfiedIndex(int i) {
            assert i >= 0 && i < this.ingredientCount;

            return this.satisfiedOffset() + i;
        }

        private void clearSatisfied() {
            this.clearRange(this.satisfiedOffset(), this.satisfiedCount());
        }

        private void setConnection(int i, int j) {
            this.data.set(this.getConnectionIndex(i, j));
        }

        private boolean hasConnection(int i, int j) {
            return this.data.get(this.getConnectionIndex(i, j));
        }

        private int getConnectionIndex(int i, int j) {
            assert i >= 0 && i < this.itemCount;

            assert j >= 0 && j < this.ingredientCount;

            return this.connectionOffset() + i * this.ingredientCount + j;
        }

        private boolean isAssigned(int i, int j) {
            return this.data.get(this.getResidualIndex(i, j));
        }

        private void assign(int i, int j) {
            int k = this.getResidualIndex(i, j);

            assert !this.data.get(k);

            this.data.set(k);
        }

        private void unassign(int i, int j) {
            int k = this.getResidualIndex(i, j);

            assert this.data.get(k);

            this.data.clear(k);
        }

        private int getResidualIndex(int i, int j) {
            assert i >= 0 && i < this.itemCount;

            assert j >= 0 && j < this.ingredientCount;

            return this.residualOffset() + i * this.ingredientCount + j;
        }

        private void visitIngredient(int i) {
            this.data.set(this.getVisitedIngredientIndex(i));
        }

        private boolean hasVisitedIngredient(int i) {
            return this.data.get(this.getVisitedIngredientIndex(i));
        }

        private int getVisitedIngredientIndex(int i) {
            assert i >= 0 && i < this.ingredientCount;

            return this.visitedIngredientOffset() + i;
        }

        private void visitItem(int i) {
            this.data.set(this.getVisitiedItemIndex(i));
        }

        private boolean hasVisitedItem(int i) {
            return this.data.get(this.getVisitiedItemIndex(i));
        }

        private int getVisitiedItemIndex(int i) {
            assert i >= 0 && i < this.itemCount;

            return this.visitedItemOffset() + i;
        }

        private void clearAllVisited() {
            this.clearRange(this.visitedIngredientOffset(), this.visitedIngredientCount());
            this.clearRange(this.visitedItemOffset(), this.visitedItemCount());
        }

        private void clearRange(int i, int j) {
            this.data.clear(i, i + j);
        }

        public int tryPickAll(int i, @Nullable AutoRecipeStackManager.b<T> autorecipestackmanager_b) {
            int j = 0;
            int k = Math.min(i, AutoRecipeStackManager.this.getResultUpperBound(this.ingredients)) + 1;

            while (true) {
                while (true) {
                    int l = (j + k) / 2;

                    if (this.tryPick(l, (AutoRecipeStackManager.b) null)) {
                        if (k - j <= 1) {
                            if (l > 0) {
                                this.tryPick(l, autorecipestackmanager_b);
                            }

                            return l;
                        }

                        j = l;
                    } else {
                        k = l;
                    }
                }
            }
        }
    }

    @FunctionalInterface
    public interface b<T> {

        void accept(T t0);
    }

    @FunctionalInterface
    public interface a<T> {

        boolean acceptsItem(T t0);
    }
}
