package mekanism.neoshim.neoforge.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;

public record SimpleModelState(Transformation getRotation, boolean isUvLocked) implements ModelState {

    public SimpleModelState(Transformation transformation) {
        this(transformation, false);
    }
}
