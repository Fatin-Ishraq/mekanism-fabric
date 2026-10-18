package mekanism.neoshim.neoforge.common.extensions;

import java.util.stream.Stream;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/** Injected into {@link EntityType}. */
public interface IEntityTypeExtension {

    @SuppressWarnings({"unchecked", "deprecation"})
    default Stream<TagKey<EntityType<?>>> getTags() {
        return ((EntityType<?>) this).builtInRegistryHolder().tags();
    }
}
