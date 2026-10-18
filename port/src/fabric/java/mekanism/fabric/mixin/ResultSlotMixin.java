package mekanism.fabric.mixin;

import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's PlayerEvent.ItemCraftedEvent for crafting grid results. */
@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {

    @Shadow
    @Final
    private Player player;
    @Shadow
    @Final
    private CraftingContainer craftSlots;

    @Inject(method = "checkTakeAchievements", at = @At("HEAD"))
    private void mekanism$itemCrafted(ItemStack stack, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, stack, craftSlots));
    }
}
