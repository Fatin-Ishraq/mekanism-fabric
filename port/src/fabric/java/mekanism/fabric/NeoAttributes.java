package mekanism.fabric;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * The NeoForge attributes Mekanism's gear modifies ({@code neoforge:swim_speed}, {@code neoforge:creative_flight}). They are registered from the end of
 * {@link net.minecraft.world.entity.ai.attributes.Attributes}' static init (AttributesMixin) so they exist before vanilla builds its default attribute suppliers and
 * before the built-in registries freeze. Effects are applied by LivingEntityHooksMixin and PlayerHooksMixin.
 */
public final class NeoAttributes {

    public static final Holder<Attribute> SWIM_SPEED = register("swim_speed", new RangedAttribute("neoforge.swim_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    public static final Holder<Attribute> CREATIVE_FLIGHT = register("creative_flight", new RangedAttribute("neoforge.creative_flight", 0.0D, 0.0D, 1.0D).setSyncable(true));

    private NeoAttributes() {
    }

    public static void init() {
        //Triggers static init
    }

    private static Holder<Attribute> register(String name, Attribute attribute) {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, ResourceLocation.fromNamespaceAndPath("neoforge", name), attribute);
    }
}
