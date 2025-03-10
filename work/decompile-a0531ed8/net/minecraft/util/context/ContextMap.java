package net.minecraft.util.context;

import com.google.common.collect.Sets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;
import org.jetbrains.annotations.Contract;

public class ContextMap {

    private final Map<ContextKey<?>, Object> params;

    ContextMap(Map<ContextKey<?>, Object> map) {
        this.params = map;
    }

    public boolean has(ContextKey<?> contextkey) {
        return this.params.containsKey(contextkey);
    }

    public <T> T getOrThrow(ContextKey<T> contextkey) {
        T t0 = this.params.get(contextkey);

        if (t0 == null) {
            throw new NoSuchElementException(contextkey.name().toString());
        } else {
            return t0;
        }
    }

    @Nullable
    public <T> T getOptional(ContextKey<T> contextkey) {
        return this.params.get(contextkey);
    }

    @Nullable
    @Contract("_,!null->!null; _,_->_")
    public <T> T getOrDefault(ContextKey<T> contextkey, @Nullable T t0) {
        return this.params.getOrDefault(contextkey, t0);
    }

    public static class a {

        private final Map<ContextKey<?>, Object> params = new IdentityHashMap();

        public a() {}

        public <T> ContextMap.a withParameter(ContextKey<T> contextkey, T t0) {
            this.params.put(contextkey, t0);
            return this;
        }

        public <T> ContextMap.a withOptionalParameter(ContextKey<T> contextkey, @Nullable T t0) {
            if (t0 == null) {
                this.params.remove(contextkey);
            } else {
                this.params.put(contextkey, t0);
            }

            return this;
        }

        public <T> T getParameter(ContextKey<T> contextkey) {
            T t0 = this.params.get(contextkey);

            if (t0 == null) {
                throw new NoSuchElementException(contextkey.name().toString());
            } else {
                return t0;
            }
        }

        @Nullable
        public <T> T getOptionalParameter(ContextKey<T> contextkey) {
            return this.params.get(contextkey);
        }

        public ContextMap create(ContextKeySet contextkeyset) {
            Set<ContextKey<?>> set = Sets.difference(this.params.keySet(), contextkeyset.allowed());

            if (!set.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + String.valueOf(set));
            } else {
                Set<ContextKey<?>> set1 = Sets.difference(contextkeyset.required(), this.params.keySet());

                if (!set1.isEmpty()) {
                    throw new IllegalArgumentException("Missing required parameters: " + String.valueOf(set1));
                } else {
                    return new ContextMap(this.params);
                }
            }
        }
    }
}
