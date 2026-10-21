package mekanism.neoshim.neoforge.registries.holdersets;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

/** NeoForge's union holder set: contains every holder that any of the given sets contains. */
public class OrHolderSet<T> implements HolderSet<T> {

    private final List<HolderSet<T>> sets;
    private Set<Holder<T>> contents;

    @SafeVarargs
    public OrHolderSet(HolderSet<T>... sets) {
        this(List.of(sets));
    }

    public OrHolderSet(List<HolderSet<T>> sets) {
        this.sets = List.copyOf(sets);
    }

    private Set<Holder<T>> contents() {
        if (contents == null) {
            Set<Holder<T>> result = new LinkedHashSet<>();
            for (HolderSet<T> set : sets) {
                for (Holder<T> holder : set) {
                    result.add(holder);
                }
            }
            contents = result;
        }
        return contents;
    }

    @Override
    public java.util.Iterator<Holder<T>> iterator() {
        return contents().iterator();
    }

    @Override
    public Stream<Holder<T>> stream() {
        return contents().stream();
    }

    @Override
    public int size() {
        return contents().size();
    }

    @Override
    public boolean isBound() {
        for (HolderSet<T> set : sets) {
            if (set.isBound()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(new ArrayList<>(contents()));
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        List<Holder<T>> list = new ArrayList<>(contents());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(random.nextInt(list.size())));
    }

    @Override
    public Holder<T> get(int index) {
        return new ArrayList<>(contents()).get(index);
    }

    @Override
    public boolean contains(Holder<T> holder) {
        for (HolderSet<T> set : sets) {
            if (set.contains(holder)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        for (HolderSet<T> set : sets) {
            if (!set.canSerializeIn(owner)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "OrHolderSet[" + sets + "]";
    }
}
