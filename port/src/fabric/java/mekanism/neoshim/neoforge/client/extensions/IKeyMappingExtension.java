package mekanism.neoshim.neoforge.client.extensions;

import com.mojang.blaze3d.platform.InputConstants;
import mekanism.neoshim.neoforge.client.settings.IKeyConflictContext;
import mekanism.neoshim.neoforge.client.settings.KeyConflictContext;
import mekanism.neoshim.neoforge.client.settings.KeyModifier;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

/** Injected into {@link KeyMapping}. Conflict contexts/modifiers are stored by KeyMappingMixin. */
public interface IKeyMappingExtension {

    private KeyMapping self() {
        return (KeyMapping) this;
    }

    default InputConstants.Key getKey() {
        return KeyBindingHelper.getBoundKeyOf(self());
    }

    default IKeyConflictContext getKeyConflictContext() {
        return KeyConflictContext.UNIVERSAL;
    }

    default void setKeyConflictContext(IKeyConflictContext keyConflictContext) {
        throw new IllegalStateException("Implemented by mixin");
    }

    default KeyModifier getKeyModifier() {
        return KeyModifier.NONE;
    }

    default KeyModifier getDefaultKeyModifier() {
        return KeyModifier.NONE;
    }

    default void setKeyModifierAndCode(KeyModifier keyModifier, InputConstants.Key keyCode) {
        self().setKey(keyCode);
    }

    default boolean isConflictContextAndModifierActive() {
        return getKeyConflictContext().isActive() && getKeyModifier().isActive(getKeyConflictContext());
    }

    default boolean isActiveAndMatches(InputConstants.Key keyCode) {
        return keyCode != InputConstants.UNKNOWN && keyCode.equals(getKey()) && isConflictContextAndModifierActive();
    }
}
