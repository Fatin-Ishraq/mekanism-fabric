package mekanism.neoshim.neoforge.server.permission.nodes;

import net.minecraft.network.chat.Component;

public final class PermissionTypes {

    public static final PermissionType<Boolean> BOOLEAN = new PermissionType<>(Boolean.class, "boolean");
    public static final PermissionType<Integer> INTEGER = new PermissionType<>(Integer.class, "integer");
    public static final PermissionType<String> STRING = new PermissionType<>(String.class, "string");
    public static final PermissionType<Component> COMPONENT = new PermissionType<>(Component.class, "text_component");

    private PermissionTypes() {
    }
}
