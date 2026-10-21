package mekanism.neoshim.neoforge.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/** Collected at init by the bridge and applied through Fabric's DefaultItemComponentEvents.MODIFY. */
public class ModifyDefaultComponentsEvent extends Event implements IModBusEvent {

    @FunctionalInterface
    public interface Initializer {

        void run(DataComponentMap.Builder builder, HolderLookup.Provider registries, Item item);
    }

    public record Modification(Predicate<Item> filter, Initializer initializer) {
    }

    private final List<Modification> modifications = new ArrayList<>();

    public void modify(ItemLike item, Initializer initializer) {
        Item target = item.asItem();
        modifications.add(new Modification(i -> i == target, initializer));
    }

    public void modify(ItemLike item, Consumer<DataComponentMap.Builder> patch) {
        modify(item, (builder, registries, target) -> patch.accept(builder));
    }

    public void modifyMatching(Predicate<? super Item> predicate, Initializer initializer) {
        modifications.add(new Modification(predicate::test, initializer));
    }

    public void modifyMatching(Predicate<? super Item> predicate, Consumer<DataComponentMap.Builder> patch) {
        modifyMatching(predicate, (builder, registries, target) -> patch.accept(builder));
    }

    public Stream<Item> getAllItems() {
        return BuiltInRegistries.ITEM.stream();
    }

    public List<Modification> getModifications() {
        return modifications;
    }
}
