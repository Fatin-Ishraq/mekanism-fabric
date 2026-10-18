package mekanism.neoshim.neoforge.common;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

/**
 * Spawn egg whose entity type is resolved lazily. Vanilla's SpawnEggItem needs the type at construction; the port registers entity types
 * before items (BuiltInRegistries order), so resolving the supplier here is safe.
 */
public class DeferredSpawnEggItem extends SpawnEggItem {

    private final Supplier<? extends EntityType<? extends Mob>> typeSupplier;

    public DeferredSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Properties props) {
        super(type.get(), backgroundColor, highlightColor, props);
        this.typeSupplier = type;
    }

    @Override
    public EntityType<?> getType(ItemStack stack) {
        EntityType<?> type = super.getType(stack);
        return type != null ? type : typeSupplier.get();
    }
}
