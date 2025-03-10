package net.minecraft.util.context;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;

public class ContextKeySet {

    private final Set<ContextKey<?>> required;
    private final Set<ContextKey<?>> allowed;

    ContextKeySet(Set<ContextKey<?>> set, Set<ContextKey<?>> set1) {
        this.required = Set.copyOf(set);
        this.allowed = Set.copyOf(Sets.union(set, set1));
    }

    public Set<ContextKey<?>> required() {
        return this.required;
    }

    public Set<ContextKey<?>> allowed() {
        return this.allowed;
    }

    public String toString() {
        Joiner joiner = Joiner.on(", ");
        Iterator iterator = this.allowed.stream().map((contextkey) -> {
            String s = this.required.contains(contextkey) ? "!" : "";

            return s + String.valueOf(contextkey.name());
        }).iterator();

        return "[" + joiner.join(iterator) + "]";
    }

    public static class a {

        private final Set<ContextKey<?>> required = Sets.newIdentityHashSet();
        private final Set<ContextKey<?>> optional = Sets.newIdentityHashSet();

        public a() {}

        public ContextKeySet.a required(ContextKey<?> contextkey) {
            if (this.optional.contains(contextkey)) {
                throw new IllegalArgumentException("Parameter " + String.valueOf(contextkey.name()) + " is already optional");
            } else {
                this.required.add(contextkey);
                return this;
            }
        }

        public ContextKeySet.a optional(ContextKey<?> contextkey) {
            if (this.required.contains(contextkey)) {
                throw new IllegalArgumentException("Parameter " + String.valueOf(contextkey.name()) + " is already required");
            } else {
                this.optional.add(contextkey);
                return this;
            }
        }

        public ContextKeySet build() {
            return new ContextKeySet(this.required, this.optional);
        }
    }
}
