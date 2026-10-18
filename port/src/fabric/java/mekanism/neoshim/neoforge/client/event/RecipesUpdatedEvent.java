package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import net.minecraft.world.item.crafting.RecipeManager;

public class RecipesUpdatedEvent extends Event {

    private final RecipeManager recipeManager;

    public RecipesUpdatedEvent(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
}
