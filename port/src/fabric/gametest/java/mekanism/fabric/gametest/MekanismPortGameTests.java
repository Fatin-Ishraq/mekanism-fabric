package mekanism.fabric.gametest;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.neoshim.neoforge.capabilities.Capabilities;
import mekanism.neoshim.neoforge.items.IItemHandler;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import team.reborn.energy.api.EnergyStorage;

/**
 * Headless functional checks for the Fabric port (run with {@code gradlew runGametest}). They exercise real Mekanism block entities through
 * the Fabric Transfer API / Team Reborn Energy bridges, recipes, ticking and data maps.
 */
public class MekanismPortGameTests implements FabricGameTest {

    private static final BlockPos POS = new BlockPos(1, 1, 1);

    @GameTest(template = EMPTY_STRUCTURE)
    public void machineBlockEntityTicks(GameTestHelper helper) {
        helper.setBlock(POS, MekanismBlocks.ENRICHMENT_CHAMBER.value());
        BlockEntity be = helper.getBlockEntity(POS);
        helper.assertTrue(be instanceof TileEntityMekanism, "Enrichment chamber has no Mekanism block entity: " + be);
        helper.runAfterDelay(5, helper::succeed);
    }

    /** Places a block and applies its item's default components, like placing it from an item does (side config defaults etc.). */
    private static void placeLikeItem(GameTestHelper helper, net.minecraft.world.level.block.Block block) {
        helper.setBlock(POS, block);
        BlockEntity be = helper.getBlockEntity(POS);
        be.applyComponentsFromItemStack(new ItemStack(block));
        be.setChanged();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 400)
    public void enrichmentChamberProcessesViaTransferApi(GameTestHelper helper) {
        placeLikeItem(helper, MekanismBlocks.ENRICHMENT_CHAMBER.value());
        BlockPos absolute = helper.absolutePos(POS);
        //Energy through Team Reborn Energy (try every side; machines only accept energy on their configured input sides)
        long totalInserted = 0;
        StringBuilder report = new StringBuilder();
        for (Direction side : Direction.values()) {
            EnergyStorage energy = EnergyStorage.SIDED.find(helper.getLevel(), absolute, side);
            report.append(side).append('=').append(energy == null ? "none" : energy.getAmount() + "/" + energy.getCapacity())
                  .append(",strict=").append(mekanism.common.capabilities.Capabilities.STRICT_ENERGY.block().getCapability(helper.getLevel(), absolute, null, null, side))
                  .append(",fe=").append(Capabilities.EnergyStorage.BLOCK.getCapability(helper.getLevel(), absolute, null, null, side)).append(' ');
            if (energy != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    totalInserted += energy.insert(1_000_000, transaction);
                    transaction.commit();
                }
            }
        }
        report.append(" null-side strict=").append(mekanism.common.capabilities.Capabilities.STRICT_ENERGY.block().getCapability(helper.getLevel(), absolute, null, null, null))
              .append(" items(up)=").append(Capabilities.ItemHandler.BLOCK.getCapability(helper.getLevel(), absolute, null, null, Direction.UP))
              .append(" be=").append(helper.getLevel().getBlockEntity(absolute))
              .append(" facing=").append(((TileEntityMekanism) helper.getLevel().getBlockEntity(absolute)).getDirection())
              .append(" energyConfig=").append(describeEnergyConfig((TileEntityMekanism) helper.getLevel().getBlockEntity(absolute)))
              .append(" canHandleEnergy=").append(((TileEntityMekanism) helper.getLevel().getBlockEntity(absolute)).canHandleEnergy());
        helper.assertTrue(totalInserted > 0, "Enrichment chamber accepted no energy: " + report);
        //Input through the Fabric item Transfer API
        Storage<ItemVariant> items = ItemStorage.SIDED.find(helper.getLevel(), absolute, Direction.UP);
        helper.assertTrue(items != null, "No item storage exposed by the enrichment chamber");
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = items.insert(ItemVariant.of(Items.IRON_ORE), 1, transaction);
            helper.assertTrue(inserted == 1, "Could not insert iron ore (inserted " + inserted + ")");
            transaction.commit();
        }
        helper.succeedWhen(() -> {
            IItemHandler handler = Capabilities.ItemHandler.BLOCK.getCapability(helper.getLevel(), absolute, null, null, null);
            helper.assertTrue(handler != null, "No item handler");
            boolean found = false;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.getItem().builtInRegistryHolder().key().location().getPath().equals("dust_iron") && stack.getCount() == 2) {
                    found = true;
                }
            }
            helper.assertTrue(found, "Iron ore was not enriched into 2 iron dust yet");
        });
    }

    private static String describeEnergyConfig(TileEntityMekanism tile) {
        if (!(tile instanceof mekanism.common.tile.interfaces.ISideConfiguration config)) {
            return "not configurable";
        }
        var info = config.getConfig().getConfig(mekanism.common.lib.transmitter.TransmissionType.ENERGY);
        if (info == null) {
            return "no energy config";
        }
        StringBuilder builder = new StringBuilder();
        for (mekanism.api.RelativeSide side : mekanism.api.RelativeSide.values()) {
            builder.append(side).append(':').append(info.getDataType(side)).append(':').append(info.getSlotInfo(side)).append(';');
        }
        return builder.toString();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void fluidTankAcceptsFabricFluids(GameTestHelper helper) {
        placeLikeItem(helper, MekanismBlocks.BASIC_FLUID_TANK.value());
        BlockPos absolute = helper.absolutePos(POS);
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(helper.getLevel(), absolute, Direction.UP);
        helper.assertTrue(storage != null, "No fluid storage exposed by the fluid tank");
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET * 2, transaction);
            helper.assertTrue(inserted == FluidConstants.BUCKET * 2, "Inserted " + inserted + " droplets instead of 2 buckets");
            transaction.commit();
        }
        long stored = 0;
        for (StorageView<FluidVariant> view : storage) {
            stored += view.getAmount();
        }
        helper.assertTrue(stored == FluidConstants.BUCKET * 2, "Tank reports " + stored + " droplets");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void vanillaChestVisibleAsItemHandler(GameTestHelper helper) {
        helper.setBlock(POS, Blocks.CHEST);
        BlockPos absolute = helper.absolutePos(POS);
        IItemHandler handler = Capabilities.ItemHandler.BLOCK.getCapability(helper.getLevel(), absolute, null, null, Direction.UP);
        helper.assertTrue(handler != null, "Vanilla chest is not visible through the NeoForge item capability bridge");
        ItemStack remainder = handler.insertItem(0, new ItemStack(Items.DIAMOND, 5), false);
        helper.assertTrue(remainder.isEmpty(), "Chest did not accept diamonds");
        helper.assertTrue(handler.getStackInSlot(0).getCount() == 5, "Chest slot does not contain the diamonds");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void chemicalDataMapsLoaded(GameTestHelper helper) {
        Holder<Chemical> hydrogen = MekanismChemicals.HYDROGEN;
        Object fuel = hydrogen.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel());
        helper.assertTrue(fuel != null, "Hydrogen has no chemical fuel data map entry");
        helper.assertTrue(MekanismAPI.CHEMICAL_REGISTRY.size() > 50, "Chemical registry looks empty");
        helper.succeed();
    }
}
