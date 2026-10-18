package mekanism.common.content.network.distribution;

import mekanism.common.content.network.transmitter.BufferedResourceTransmitter;
import mekanism.common.lib.distribution.Target;
import mekanism.neoshim.neoforge.transfer.resource.Resource;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;

public class ResourceTransmitterSaveTarget<RESOURCE extends Resource> extends Target<BufferedResourceTransmitter<RESOURCE, ?, ?, ?>.SaveShareJournal, RESOURCE> {

    public ResourceTransmitterSaveTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected long accept(BufferedResourceTransmitter<RESOURCE, ?, ?, ?>.SaveShareJournal handler, RESOURCE resource, long amount, TransactionContext transaction) {
        return handler.accept(resource, amount, transaction);
    }
}