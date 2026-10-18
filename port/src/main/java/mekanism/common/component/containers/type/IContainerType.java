package mekanism.common.component.containers.type;

import java.util.function.Supplier;
import mekanism.common.component.containers.creator.IContainerCreator;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.neoforge.common.util.ValueIOSerializable;
import mekanism.neoshim.neoforge.registries.DeferredHolder;
import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface IContainerType<CONTAINER extends ValueIOSerializable, ATTACHED> {

    DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> getComponentType();

    default Identifier getComponentName() {
        return getComponentType().getId();
    }

    String getTag();

    void addDefaultCreators(@Nullable IEventBus eventBus, Item item, Supplier<? extends IContainerCreator<CONTAINER, ATTACHED>> defaultCreator,
          IMekanismConfig... requiredConfigs);

    /// Visible for datagen
    int getContainerCount(Item item);

    void addDefault(Item item, DataComponentMap.Builder components);

    default <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean supports(ITEM instance) {
        return instance.has(getComponentType()) || supports(instance.typeHolder());
    }

    default boolean supports(Holder<Item> item) {
        return supports(item.value());
    }

    boolean supports(Item item);

    @Nullable
    default ATTACHED get(DataComponentGetter componentGetter) {
        return componentGetter.get(getComponentType());
    }

    //TODO: Add some sort of note about how the returned containers entirely ignore the size of the item access
    CONTAINER createContainer(ItemAccess attachedAccess, int containerIndex);

    @Nullable
    ATTACHED createNewAttachment(ItemResource itemType);

    boolean canHandle(TileEntityMekanism tile);

    void saveTo(ValueOutput output, TileEntityMekanism tile);

    void readFrom(ValueInput input, TileEntityMekanism tile);

    void copy(CONTAINER from, CONTAINER to, @Nullable TransactionContext transaction);

    void copyToTile(TileEntityMekanism tile, DataComponentGetter componentGetter);

    void copyFromTile(TileEntityMekanism tile, DataComponentMap.Builder builder);
}