package mekanism.neoshim.neoforge.transfer.resource;

/** A resource with an amount. */
public record ResourceStack<T extends Resource>(T resource, int amount) {

    public boolean isEmpty() {
        return resource.isEmpty() || amount <= 0;
    }

    @Override
    public String toString() {
        return amount + " " + resource;
    }
}
