package mekanism.neoshim.neoforge.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mekanism.neoshim.bus.api.Event;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Fired from a mixin on ItemStack attribute collection (NOTES.md "Hook status"). */
public class ItemAttributeModifierEvent extends Event {

    private final ItemStack stack;
    private final ItemAttributeModifiers defaultModifiers;
    private List<ItemAttributeModifiers.Entry> entries;

    public ItemAttributeModifierEvent(ItemStack stack, ItemAttributeModifiers defaultModifiers) {
        this.stack = stack;
        this.defaultModifiers = defaultModifiers;
        this.entries = new ArrayList<>(defaultModifiers.modifiers());
    }

    public ItemStack getItemStack() {
        return stack;
    }

    public ItemAttributeModifiers getDefaultModifiers() {
        return defaultModifiers;
    }

    public List<ItemAttributeModifiers.Entry> getModifiers() {
        return List.copyOf(entries);
    }

    public boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        for (ItemAttributeModifiers.Entry entry : entries) {
            if (entry.attribute().equals(attribute) && entry.modifier().id().equals(modifier.id())) {
                return false;
            }
        }
        entries.add(new ItemAttributeModifiers.Entry(attribute, modifier, slot));
        return true;
    }

    public void replaceModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        entries.removeIf(entry -> entry.attribute().equals(attribute) && entry.modifier().id().equals(modifier.id()));
        entries.add(new ItemAttributeModifiers.Entry(attribute, modifier, slot));
    }

    public boolean removeModifier(Holder<Attribute> attribute, net.minecraft.resources.Identifier id) {
        return entries.removeIf(entry -> entry.attribute().equals(attribute) && entry.modifier().id().equals(id));
    }

    public boolean removeIf(Predicate<ItemAttributeModifiers.Entry> condition) {
        return entries.removeIf(condition);
    }

    public void removeAllModifiersFor(Holder<Attribute> attribute) {
        entries.removeIf(entry -> entry.attribute().equals(attribute));
    }

    public void clearModifiers() {
        entries.clear();
    }

    public ItemAttributeModifiers build() {
        return new ItemAttributeModifiers(List.copyOf(entries), defaultModifiers.showInTooltip());
    }
}
