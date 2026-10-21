package mekanism.neoshim.neoforge.client.event;

import java.util.Set;
import mekanism.neoshim.bus.api.Event;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

/** Client-side recipe synchronization, bridged from Fabric's {@code ClientRecipeSynchronizedEvent}. */
public class RecipesReceivedEvent extends Event {

    private final Set<RecipeType<?>> recipeTypes;
    private final RecipeMap recipeMap;

    public RecipesReceivedEvent(Set<RecipeType<?>> recipeTypes, RecipeMap recipeMap) {
        this.recipeTypes = recipeTypes;
        this.recipeMap = recipeMap;
    }

    public Set<RecipeType<?>> getRecipeTypes() {
        return recipeTypes;
    }

    public RecipeMap getRecipeMap() {
        return recipeMap;
    }
}
