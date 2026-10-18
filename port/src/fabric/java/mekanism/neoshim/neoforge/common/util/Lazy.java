package mekanism.neoshim.neoforge.common.util;

import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public interface Lazy<T> extends Supplier<T> {

    static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>() {
            @Nullable
            private Supplier<T> delegate = supplier;
            @Nullable
            private T value;

            @Override
            public T get() {
                if (delegate != null) {
                    value = delegate.get();
                    delegate = null;
                }
                return value;
            }

            @Override
            public void invalidate() {
                //Not supported for simple lazies
            }
        };
    }

    static <T> Lazy<T> concurrentOf(Supplier<T> supplier) {
        Lazy<T> inner = of(supplier);
        return new Lazy<>() {
            @Override
            public synchronized T get() {
                return inner.get();
            }

            @Override
            public void invalidate() {
            }
        };
    }

    void invalidate();
}
