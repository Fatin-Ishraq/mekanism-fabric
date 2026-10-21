package mekanism.neoshim.neoforge.registries.holdersets;

import com.mojang.datafixers.util.Either;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

/** HolderSet containing every element of a registry. */
public record AnyHolderSet<T>(HolderLookup.RegistryLookup<T> registryLookup) implements HolderSet<T> {

    private List<Holder<T>> list() {
        return registryLookup.listElements().map(h -> (Holder<T>) h).toList();
    }

    @Override
    public Iterator<Holder<T>> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<Holder<T>> stream() {
        return registryLookup.listElements().map(h -> h);
    }

    @Override
    public int size() {
        return list().size();
    }

    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(list());
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        List<Holder<T>> list = list();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(random.nextInt(list.size())));
    }

    @Override
    public Holder<T> get(int i) {
        return list().get(i);
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return holder.unwrapKey().map(key -> registryLookup.get(key).isPresent()).orElse(false);
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return registryLookup.canSerializeIn(owner);
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }
}
