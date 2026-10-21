package mekanism.fabric.mixin.client;

import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets unbaked models that are also {@link ResolvableModel}s (such as composite models) declare dependencies besides their parent. */
@Mixin(ModelDiscovery.class)
public abstract class ModelDiscoveryMixin {

    @Shadow
    @Final
    private ResolvableModel.Resolver resolver;

    @Inject(method = "createAndQueueWrapper", at = @At("RETURN"))
    private void mekanism$resolveExtraDependencies(Identifier id, UnbakedModel rawModel, CallbackInfoReturnable<?> cir) {
        if (rawModel instanceof ResolvableModel resolvable) {
            resolvable.resolveDependencies(resolver);
        }
    }
}
