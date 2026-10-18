package mekanism.neoshim.neoforge.transfer;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.neoforge.common.util.ValueIOSerializable;
import mekanism.neoshim.neoforge.transfer.resource.Resource;
import mekanism.neoshim.neoforge.transfer.transaction.SnapshotJournal;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** A resource handler storing one "stack" object per index, with transactional changes (one journal per index). */
public abstract class StacksResourceHandler<S, T extends Resource> implements ResourceHandler<T>, ValueIOSerializable {

    public static final String VALUE_IO_KEY = "stacks";

    protected final S emptyStack;
    protected NonNullList<S> stacks;
    protected final Codec<NonNullList<S>> codec;
    private final List<StackJournal> journals = new ArrayList<>();

    protected StacksResourceHandler(int size, S emptyStack, Codec<S> stackCodec) {
        this(NonNullList.withSize(size, emptyStack), emptyStack, stackCodec);
    }

    protected StacksResourceHandler(NonNullList<S> stacks, S emptyStack, Codec<S> stackCodec) {
        this.emptyStack = emptyStack;
        this.stacks = stacks;
        this.codec = stackCodec.listOf().xmap(list -> {
            NonNullList<S> result = NonNullList.withSize(list.size(), emptyStack);
            for (int i = 0; i < list.size(); i++) {
                result.set(i, list.get(i));
            }
            return result;
        }, list -> list);
    }

    protected void setStacks(NonNullList<S> stacks) {
        this.stacks = stacks;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.store(VALUE_IO_KEY, codec, stacks);
    }

    @Override
    public void deserialize(ValueInput input) {
        input.read(VALUE_IO_KEY, codec).ifPresent(this::setStacks);
    }

    public void set(int index, T resource, int amount) {
        stacks.set(index, resource.isEmpty() || amount <= 0 ? emptyStack : getStackFrom(resource, amount));
    }

    protected abstract T getResourceFrom(S stack);

    protected abstract int getAmountFrom(S stack);

    protected abstract S getStackFrom(T resource, int amount);

    protected abstract S copyOf(S stack);

    protected boolean matches(S stack, T resource) {
        return getResourceFrom(stack).equals(resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        return true;
    }

    protected abstract int getCapacity(int index, T resource);

    protected void onContentsChanged(int index, S previousContents) {
    }

    public NonNullList<S> copyToList() {
        NonNullList<S> copy = NonNullList.withSize(stacks.size(), emptyStack);
        for (int i = 0; i < stacks.size(); i++) {
            copy.set(i, copyOf(stacks.get(i)));
        }
        return copy;
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public T getResource(int index) {
        return getResourceFrom(stacks.get(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return getAmountFrom(stacks.get(index));
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        return getCapacity(index, resource);
    }

    private StackJournal journal(int index) {
        while (journals.size() <= index) {
            journals.add(new StackJournal(journals.size()));
        }
        return journals.get(index);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !isValid(index, resource)) {
            return 0;
        }
        S current = stacks.get(index);
        int currentAmount = getAmountFrom(current);
        if (currentAmount > 0 && !matches(current, resource)) {
            return 0;
        }
        int inserted = Math.min(amount, getCapacity(index, resource) - currentAmount);
        if (inserted <= 0) {
            return 0;
        }
        journal(index).updateSnapshots(transaction);
        stacks.set(index, getStackFrom(resource, currentAmount + inserted));
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        S current = stacks.get(index);
        int currentAmount = getAmountFrom(current);
        if (amount == 0 || currentAmount <= 0 || !matches(current, resource)) {
            return 0;
        }
        int extracted = Math.min(amount, currentAmount);
        journal(index).updateSnapshots(transaction);
        stacks.set(index, extracted == currentAmount ? emptyStack : getStackFrom(resource, currentAmount - extracted));
        return extracted;
    }

    private final class StackJournal extends SnapshotJournal<S> {

        private final int index;

        private StackJournal(int index) {
            this.index = index;
        }

        @Override
        protected S createSnapshot() {
            return copyOf(stacks.get(index));
        }

        @Override
        protected void revertToSnapshot(S snapshot) {
            stacks.set(index, snapshot);
        }

        @Override
        protected void onRootCommit(S originalState) {
            onContentsChanged(index, originalState);
        }
    }
}
