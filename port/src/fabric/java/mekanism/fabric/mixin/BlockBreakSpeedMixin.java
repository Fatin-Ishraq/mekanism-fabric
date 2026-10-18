package mekanism.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** NeoForge's PlayerEvent.BreakSpeed, at the one vanilla call site of Player#getDestroySpeed that knows the position. */
@Mixin(BlockBehaviour.class)
public abstract class BlockBreakSpeedMixin {

    @WrapOperation(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"))
    private float mekanism$breakSpeed(Player player, BlockState state, Operation<Float> original, BlockState stateArg, Player playerArg, BlockGetter level,
          BlockPos pos) {
        PlayerEvent.BreakSpeed event = NeoForge.EVENT_BUS.post(new PlayerEvent.BreakSpeed(player, state, original.call(player, state), pos));
        return event.isCanceled() ? 0 : event.getNewSpeed();
    }
}
