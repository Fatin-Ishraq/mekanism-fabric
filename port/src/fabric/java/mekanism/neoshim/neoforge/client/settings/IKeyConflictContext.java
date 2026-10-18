package mekanism.neoshim.neoforge.client.settings;

public interface IKeyConflictContext {

    boolean isActive();

    boolean conflicts(IKeyConflictContext other);
}
