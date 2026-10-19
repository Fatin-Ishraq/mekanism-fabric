package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Optional;
import mekanism.neoshim.neoforge.registries.RegistryAliases;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Falls back to NeoForge-style registry aliases when an id lookup misses. */
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryAliasMixin<T> {

    @Shadow
    public abstract ResourceKey<? extends Registry<T>> key();

    @Shadow
    public abstract T get(Identifier name);

    @Shadow
    public abstract Optional<Holder.Reference<T>> getHolder(Identifier location);

    @Shadow
    public abstract boolean containsKey(Identifier name);

    @ModifyReturnValue(method = "get(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;", at = @At("RETURN"))
    private T mekanism$aliasGet(T value, Identifier name) {
        if (value == null) {
            Identifier alias = RegistryAliases.resolve(key(), name);
            if (alias != null) {
                return get(alias);
            }
        }
        return value;
    }

    @ModifyReturnValue(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", at = @At("RETURN"))
    private T mekanism$aliasGetKey(T value, ResourceKey<T> key) {
        if (value == null && key != null) {
            Identifier alias = RegistryAliases.resolve(key(), key.identifier());
            if (alias != null) {
                return get(alias);
            }
        }
        return value;
    }

    @ModifyReturnValue(method = "getHolder(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;", at = @At("RETURN"))
    private Optional<Holder.Reference<T>> mekanism$aliasHolder(Optional<Holder.Reference<T>> value, Identifier location) {
        if (value.isEmpty()) {
            Identifier alias = RegistryAliases.resolve(key(), location);
            if (alias != null) {
                return getHolder(alias);
            }
        }
        return value;
    }

    @ModifyReturnValue(method = "getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", at = @At("RETURN"))
    private Optional<Holder.Reference<T>> mekanism$aliasHolderKey(Optional<Holder.Reference<T>> value, ResourceKey<T> key) {
        if (value.isEmpty()) {
            Identifier alias = RegistryAliases.resolve(key(), key.identifier());
            if (alias != null) {
                return getHolder(alias);
            }
        }
        return value;
    }

    @ModifyReturnValue(method = "containsKey(Lnet/minecraft/resources/Identifier;)Z", at = @At("RETURN"))
    private boolean mekanism$aliasContains(boolean value, Identifier name) {
        if (!value) {
            Identifier alias = RegistryAliases.resolve(key(), name);
            return alias != null && containsKey(alias);
        }
        return true;
    }
}
