package mekanism.common.content.network.distribution;

import com.google.common.primitives.Ints;
import java.util.Collection;
import mekanism.common.lib.distribution.Target;
import mekanism.neoshim.neoforge.transfer.energy.EnergyHandler;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class EnergyHandlerTarget extends Target<EnergyHandler, @Nullable Void> {

    public EnergyHandlerTarget(Collection<EnergyHandler> allHandlers) {
        super(allHandlers);
    }

    public EnergyHandlerTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected long accept(EnergyHandler handler, @Nullable Void unused, long amount, TransactionContext transaction) {
        return handler.insert(Ints.saturatedCast(amount), transaction);
    }
}