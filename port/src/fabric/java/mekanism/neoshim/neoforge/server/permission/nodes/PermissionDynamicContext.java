package mekanism.neoshim.neoforge.server.permission.nodes;

public record PermissionDynamicContext<T>(PermissionDynamicContextKey<T> getDynamic, T getValue) {

    public String getSerializedValue() {
        return getDynamic.serializer().apply(getValue);
    }
}
