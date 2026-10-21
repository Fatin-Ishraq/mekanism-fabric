package mekanism.neoshim.neoforge.common.extensions;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;

/** Injected into {@link DataComponentHolder}: NeoForge's tooltip helper for a single data component. */
public interface IDataComponentHolderExtension {

    private DataComponentHolder self() {
        return (DataComponentHolder) this;
    }

    default <T extends TooltipProvider> void addToTooltip(DataComponentType<T> componentType, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
          Consumer<Component> tooltipAdder, TooltipFlag flag) {
        T provider = self().get(componentType);
        if (provider != null && tooltipDisplay.shows(componentType)) {
            provider.addToTooltip(context, tooltipAdder, flag, self());
        }
    }

    default <T extends TooltipProvider> void addToTooltip(Supplier<? extends DataComponentType<T>> componentType, Item.TooltipContext context,
          TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        addToTooltip(componentType.get(), context, tooltipDisplay, tooltipAdder, flag);
    }
}
