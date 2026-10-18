package mekanism.neoshim.neoforge.client.model.data;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/** Immutable property map passed from block entities to baked models. */
public final class ModelData {

    public static final ModelData EMPTY = ModelData.builder().build();

    private final Map<ModelProperty<?>, Object> properties;

    private ModelData(Map<ModelProperty<?>, Object> properties) {
        this.properties = properties;
    }

    public Set<ModelProperty<?>> getProperties() {
        return properties.keySet();
    }

    public boolean has(ModelProperty<?> property) {
        return properties.containsKey(property);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(ModelProperty<T> property) {
        return (T) properties.get(property);
    }

    public Builder derive() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder(null);
    }

    public static <T> ModelData of(ModelProperty<T> property, T value) {
        return builder().with(property, value).build();
    }

    public static final class Builder {

        private final Map<ModelProperty<?>, Object> properties = new IdentityHashMap<>();

        private Builder(@Nullable ModelData parent) {
            if (parent != null) {
                properties.putAll(parent.properties);
            }
        }

        public <T> Builder with(ModelProperty<T> property, T value) {
            if (!property.test(value)) {
                throw new IllegalArgumentException("The provided value is invalid for this property.");
            }
            properties.put(property, value);
            return this;
        }

        public ModelData build() {
            return new ModelData(Map.copyOf(properties));
        }
    }
}
