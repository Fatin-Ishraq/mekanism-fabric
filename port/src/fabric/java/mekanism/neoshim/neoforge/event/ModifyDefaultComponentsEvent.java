package mekanism.neoshim.neoforge.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/** Collected at init by the bridge and applied through Fabric's DefaultItemComponentEvents.MODIFY. */
public class ModifyDefaultComponentsEvent extends Event implements IModBusEvent {

    public record Modification(Predicate<Item> filter, Consumer<DataComponentPatch.Builder> patch) {
    }

    private final List<Modification> modifications = new ArrayList<>();

    public void modify(ItemLike item, Consumer<DataComponentPatch.Builder> patch) {
        Item target = item.asItem();
        modifications.add(new Modification(i -> i == target, patch));
    }

    public void modifyMatching(Predicate<? super Item> predicate, Consumer<DataComponentPatch.Builder> patch) {
        modifications.add(new Modification(predicate::test, patch));
    }

    public Stream<Item> getAllItems() {
        return BuiltInRegistries.ITEM.stream();
    }

    public List<Modification> getModifications() {
        return modifications;
    }
}
