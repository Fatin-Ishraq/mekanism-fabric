package mekanism.neoshim.neoforge.client.model.data;

import java.util.function.Predicate;

public class ModelProperty<T> implements Predicate<T> {

    private final Predicate<T> predicate;

    public ModelProperty() {
        this(t -> true);
    }

    public ModelProperty(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(T t) {
        return predicate.test(t);
    }
}
