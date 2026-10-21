package mekanism.fabric.mixin;

import java.util.function.BiConsumer;
import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.event.ItemAttributeModifierEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NeoForge's ItemAttributeModifierEvent (IItemStackExtension#getAttributeModifiers): both forEachModifier overloads resolve the stack's modifiers (component, else
 * the item's defaults) and let listeners adjust them before iterating, then apply enchantment modifiers exactly as vanilla does.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackAttributesMixin {

    @Unique
    private ItemAttributeModifiers mekanism$attributeModifiers() {
        ItemStack self = (ItemStack) (Object) this;
        ItemAttributeModifiers modifiers = self.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (modifiers.modifiers().isEmpty()) {
            modifiers = self.getItem().getDefaultAttributeModifiers(self);
        }
        if (self.isEmpty()) {
            return modifiers;
        }
        return NeoForge.EVENT_BUS.post(new ItemAttributeModifierEvent(self, modifiers)).build();
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void mekanism$forEachModifier(EquipmentSlotGroup slotGroup, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> action, CallbackInfo ci) {
        mekanism$attributeModifiers().forEach(slotGroup, action);
        EnchantmentHelper.forEachModifier((ItemStack) (Object) this, slotGroup,
              (attribute, modifier) -> action.accept(attribute, modifier, ItemAttributeModifiers.Display.attributeModifiers()));
        ci.cancel();
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void mekanism$forEachModifier(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
        mekanism$attributeModifiers().forEach(slot, action);
        EnchantmentHelper.forEachModifier((ItemStack) (Object) this, slot, action);
        ci.cancel();
    }
}
