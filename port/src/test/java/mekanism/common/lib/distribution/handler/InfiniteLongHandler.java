package mekanism.common.lib.distribution.handler;

import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;

public class InfiniteLongHandler extends LongHandler {

    @Override
    public long perform(long amountOffered, TransactionContext transaction) {
        return accept(amountOffered, transaction);
    }
}