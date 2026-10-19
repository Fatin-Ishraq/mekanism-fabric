package mekanism.api;

import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;

/// Provides access to a variety of different helpers that are exposed to the API.
///
/// @since 10.4.0
public interface IMekanismAccess {

    /// Provides access to Mekanism's internals.
    IMekanismAccess INSTANCE = MekanismAPI.getService(IMekanismAccess.class);

    //FABRIC: 26.1 phase 1 - jeiHelper()/emiHelper() are compiled out with the JEI and EMI integrations

    /// Gets the item stack ingredient creator.
    ///
    /// @apiNote Use [mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#item()] instead.
    IItemStackIngredientCreator itemStackIngredientCreator();

    /// Gets the fluid stack ingredient creator.
    ///
    /// @apiNote Use [mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#fluid()] instead.
    IFluidStackIngredientCreator fluidStackIngredientCreator();

    /// Gets the chemical stack ingredient creator.
    ///
    /// @apiNote Use [mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#chemicalStack()] instead.
    /// @since 10.7.0
    IChemicalStackIngredientCreator chemicalStackIngredientCreator();

    /// Gets the chemical ingredient creator.
    ///
    /// @apiNote Use [mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#chemical()] instead.
    /// @since 10.7.0
    IChemicalIngredientCreator chemicalIngredientCreator();
}