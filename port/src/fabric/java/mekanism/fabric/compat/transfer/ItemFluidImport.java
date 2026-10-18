package mekanism.fabric.compat.transfer;

import java.util.function.Function;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.capability.IFluidHandler;
import mekanism.neoshim.neoforge.fluids.capability.IFluidHandlerItem;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** A foreign Fabric item fluid storage (vanilla buckets, other mods' tanks) viewed as an {@link IFluidHandlerItem}. */
public final class ItemFluidImport implements IFluidHandlerItem {

    private ItemStack container;

    private ItemFluidImport(ItemStack container) {
        this.container = container;
    }

    /** @return null if the stack has no Fabric fluid storage. */
    public static ItemFluidImport of(ItemStack stack) {
        return FluidStorage.ITEM.find(stack, new ItemStackSlot(stack).context()) == null ? null : new ItemFluidImport(stack);
    }

    private <R> R run(Function<IFluidHandler, R> operation) {
        ItemStackSlot slot = new ItemStackSlot(container);
        ContainerItemContext context = slot.context();
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(container, context);
        if (storage == null) {
            return operation.apply(new StorageFluidHandler(Storage.empty()));
        }
        R result = operation.apply(new StorageFluidHandler(storage));
        slot.writeBack();
        container = slot.container();
        return result;
    }

    @NotNull
    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return run(IFluidHandler::getTanks);
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return run(handler -> handler.getFluidInTank(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        return run(handler -> handler.getTankCapacity(tank));
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return run(handler -> handler.fill(resource, action));
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return run(handler -> handler.drain(resource, action));
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return run(handler -> handler.drain(maxDrain, action));
    }
}
