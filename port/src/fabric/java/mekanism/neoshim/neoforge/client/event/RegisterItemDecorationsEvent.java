package mekanism.neoshim.neoforge.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import mekanism.neoshim.neoforge.client.IItemDecorator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/** Decorators are drawn by a mixin on GuiGraphicsExtractor#renderItemDecorations (NOTES.md "Hook status"). */
public class RegisterItemDecorationsEvent extends Event implements IModBusEvent {

    public static final Map<Item, List<IItemDecorator>> DECORATORS = new ConcurrentHashMap<>();

    public void register(ItemLike itemLike, IItemDecorator decorator) {
        DECORATORS.computeIfAbsent(itemLike.asItem(), item -> new ArrayList<>()).add(decorator);
    }
}
