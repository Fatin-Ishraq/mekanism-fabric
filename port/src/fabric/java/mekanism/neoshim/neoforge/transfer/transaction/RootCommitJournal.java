package mekanism.neoshim.neoforge.transfer.transaction;

/** Runs an action once the root transaction it took part in commits. */
public final class RootCommitJournal extends SnapshotJournal<Void> {

    private final Runnable onRootCommit;

    public RootCommitJournal(Runnable onRootCommit) {
        this.onRootCommit = onRootCommit;
    }

    @Override
    protected Void createSnapshot() {
        return null;
    }

    @Override
    protected void revertToSnapshot(Void snapshot) {
    }

    @Override
    protected void onRootCommit(Void originalState) {
        onRootCommit.run();
    }
}
