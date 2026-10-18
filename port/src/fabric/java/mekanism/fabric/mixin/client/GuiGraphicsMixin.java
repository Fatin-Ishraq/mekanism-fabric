package mekanism.fabric.mixin.client;

import java.util.List;
import mekanism.neoshim.neoforge.client.IItemDecorator;
import mekanism.neoshim.neoforge.client.event.RegisterItemDecorationsEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge item decorators (RegisterItemDecorationsEvent), drawn after vanilla's decorations. */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void mekanism$itemDecorators(Font font, ItemStack stack, int x, int y, @Nullable String text, CallbackInfo ci) {
        if (stack.isEmpty()) {
            return;
        }
        List<IItemDecorator> decorators = RegisterItemDecorationsEvent.DECORATORS.get(stack.getItem());
        if (decorators != null) {
            GuiGraphics self = (GuiGraphics) (Object) this;
            for (IItemDecorator decorator : decorators) {
                decorator.render(self, font, stack, x, y);
            }
        }
    }
}
