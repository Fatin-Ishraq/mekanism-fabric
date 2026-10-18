package mekanism.fabric.mixin.client;

import mekanism.neoshim.neoforge.client.extensions.IKeyMappingExtension;
import mekanism.neoshim.neoforge.client.settings.IKeyConflictContext;
import mekanism.neoshim.neoforge.client.settings.KeyConflictContext;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements IKeyMappingExtension {

    @Unique
    private IKeyConflictContext mekanism$conflictContext = KeyConflictContext.UNIVERSAL;

    @Override
    public IKeyConflictContext getKeyConflictContext() {
        return mekanism$conflictContext;
    }

    @Override
    public void setKeyConflictContext(IKeyConflictContext keyConflictContext) {
        this.mekanism$conflictContext = keyConflictContext;
    }
}
