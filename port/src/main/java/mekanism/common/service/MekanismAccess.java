package mekanism.common.service;

import mekanism.api.IMekanismAccess;
import mekanism.api.integration.emi.IMekanismEmiHelper;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.common.recipe.ingredients.ChemicalIngredientCreator;
import mekanism.common.recipe.ingredients.creator.ChemicalStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.ItemStackIngredientCreator;

/// @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via [IMekanismAccess#INSTANCE]
public class MekanismAccess implements IMekanismAccess {

    @Override
    public IMekanismJEIHelper jeiHelper() {//FABRIC: JEI is not on the 26.1 Fabric classpath (phase 9)
        throw new IllegalStateException("JEI not included");
    }

    @Override
    public IMekanismEmiHelper emiHelper() {// TODO - 26.1: EMI
        throw new IllegalStateException("EMI not included");
        /*Mekanism.hooks.emi.assertLoaded();
        return MekanismEmiHelper.INSTANCE;*/
    }

    @Override
    public IItemStackIngredientCreator itemStackIngredientCreator() {
        return ItemStackIngredientCreator.INSTANCE;
    }

    @Override
    public IFluidStackIngredientCreator fluidStackIngredientCreator() {
        return FluidStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalStackIngredientCreator chemicalStackIngredientCreator() {
        return ChemicalStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalIngredientCreator chemicalIngredientCreator() {
        return ChemicalIngredientCreator.INSTANCE;
    }
}