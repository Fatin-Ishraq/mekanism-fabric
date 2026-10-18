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

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 300)
    public void universalCablesFormNetworkAndDeliverEnergy(GameTestHelper helper) {
        BlockPos machine = new BlockPos(1, 1, 1);
        helper.setBlock(machine, MekanismBlocks.ENRICHMENT_CHAMBER.value());
        BlockEntity machineTile = helper.getBlockEntity(machine);
        machineTile.applyComponentsFromItemStack(new ItemStack(MekanismBlocks.ENRICHMENT_CHAMBER.value()));
        BlockPos cableA = new BlockPos(2, 1, 1);
        BlockPos cableB = new BlockPos(3, 1, 1);
        helper.setBlock(cableA, MekanismBlocks.BASIC_UNIVERSAL_CABLE.value());
        helper.setBlock(cableB, MekanismBlocks.BASIC_UNIVERSAL_CABLE.value());
        helper.runAfterDelay(5, () -> {
            var transmitter = ((mekanism.common.tile.transmitter.TileEntityTransmitter) helper.getBlockEntity(cableA)).getTransmitter();
            helper.assertTrue(transmitter.hasTransmitterNetwork(), "Universal cable did not form a network");
            helper.assertTrue(transmitter.getTransmitterNetwork().transmittersSize() == 2,
                  "Cable network has " + transmitter.getTransmitterNetwork().transmittersSize() + " transmitters instead of 2");
            //Real energy source: a creative energy cube whose front (output by default) faces the cable network
            BlockPos cube = new BlockPos(4, 1, 1);
            var cubeState = MekanismBlocks.CREATIVE_ENERGY_CUBE.value().defaultBlockState();
            for (var property : cubeState.getProperties()) {
                if (property.getName().equals("facing")) {
                    cubeState = cubeState.setValue((net.minecraft.world.level.block.state.properties.DirectionProperty) property, Direction.WEST);
                }
            }
            helper.setBlock(cube, cubeState);
            helper.getBlockEntity(cube).applyComponentsFromItemStack(new ItemStack(MekanismBlocks.CREATIVE_ENERGY_CUBE.value()));
            //Fill it (the "filled" creative cube only exists as a creative-tab item variant)
            ((mekanism.common.tile.TileEntityEnergyCube) helper.getBlockEntity(cube)).getEnergyContainer().setEnergy(Long.MAX_VALUE);
            helper.succeedWhen(() -> {
                var strict = mekanism.common.capabilities.Capabilities.STRICT_ENERGY.block().getCapability(helper.getLevel(), helper.absolutePos(machine), null, null, null);
                if (strict == null || strict.getEnergy(0) <= 0) {
                    var cubeStrict = mekanism.common.capabilities.Capabilities.STRICT_ENERGY.block().getCapability(helper.getLevel(), helper.absolutePos(cube), null, null, null);
                    var cableB2 = (mekanism.common.content.network.transmitter.UniversalCable) ((mekanism.common.tile.transmitter.TileEntityTransmitter) helper.getBlockEntity(cableB)).getTransmitter();
                    var cableA2 = (mekanism.common.content.network.transmitter.UniversalCable) ((mekanism.common.tile.transmitter.TileEntityTransmitter) helper.getBlockEntity(cableA)).getTransmitter();
                    var cubeFacing = ((TileEntityMekanism) helper.getBlockEntity(cube)).getDirection();
                    var cubeWest = mekanism.common.capabilities.Capabilities.STRICT_ENERGY.block().getCapability(helper.getLevel(), helper.absolutePos(cube), null, null, Direction.WEST);
                    helper.fail("Energy did not reach the machine: cube=" + (cubeStrict == null ? "null" : cubeStrict.getEnergy(0) + "/" + cubeStrict.getMaxEnergy(0))
                                + " cubeFacing=" + cubeFacing + " cubeWestCap=" + cubeWest
                                + " cableB east=" + cableB2.getConnectionType(Direction.EAST) + " cableA west=" + cableA2.getConnectionType(Direction.WEST)
                                + " net energy=" + (cableB2.hasTransmitterNetwork() ? cableB2.getTransmitterNetwork().energyContainer.getEnergy() + " acceptors=" + cableB2.getTransmitterNetwork().getAcceptorCount() : "none"));
                }
            });
        });
    }


    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 200)
    public void dynamicTankMultiblockForms(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) {
            for (int y = 1; y < 4; y++) {
                for (int z = 0; z < 3; z++) {
                    boolean interior = x == 1 && y == 2 && z == 1;
                    if (!interior) {
                        helper.setBlock(new BlockPos(x, y, z), MekanismBlocks.DYNAMIC_TANK.value());
                    }
                }
            }
        }
        helper.succeedWhen(() -> {
            var tile = (mekanism.common.tile.prefab.TileEntityMultiblock<?>) helper.getBlockEntity(new BlockPos(0, 1, 0));
            helper.assertTrue(tile.getMultiblock().isFormed(), "Dynamic tank did not form");
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void oresAddedToOverworldBiomes(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        var plains = biomes.getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS);
        boolean found = plains.getGenerationSettings().features().stream()
              .flatMap(set -> set.stream())
              .anyMatch(feature -> feature.unwrapKey().map(key -> key.location().toString().equals("mekanism:ore_osmium_upper")).orElse(false));
        helper.assertTrue(found, "Plains biome does not generate Mekanism osmium ore");
        helper.succeed();
    }

    /** ItemAttributeModifierEvent: the Meka-Tool has no static attribute modifiers, its attack damage comes only from the event. */
    @GameTest(template = EMPTY_STRUCTURE)
    public void mekaToolAttributesFromEvent(GameTestHelper helper) {
        ItemStack mekaTool = new ItemStack(mekanism.common.registries.MekanismItems.MEKA_TOOL.get());
        double[] attackDamage = {0};
        mekaTool.forEachModifier(net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
                attackDamage[0] += modifier.amount();
            }
        });
        helper.assertTrue(attackDamage[0] > 0, "Meka-Tool has no attack damage modifier (ItemAttributeModifierEvent not fired)");
        helper.succeed();
    }

    /** LivingFallEvent: charged free runners absorb fall damage, an unequipped mob takes it. */
    @GameTest(template = EMPTY_STRUCTURE)
    public void freeRunnersCancelFallDamage(GameTestHelper helper) {
        net.minecraft.world.entity.animal.Pig control = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(1, 2, 1));
        net.minecraft.world.entity.animal.Pig runner = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(3, 2, 3));
        ItemStack freeRunners = new ItemStack(mekanism.common.registries.MekanismItems.FREE_RUNNERS.get());
        mekanism.api.energy.IEnergyContainer container = mekanism.common.util.StorageUtils.getEnergyContainer(freeRunners, 0);
        helper.assertTrue(container != null, "Free runners have no energy container");
        container.setEnergy(container.getMaxEnergy());
        runner.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, freeRunners);
        control.causeFallDamage(8, 1, control.damageSources().fall());
        runner.causeFallDamage(8, 1, runner.damageSources().fall());
        helper.assertTrue(control.getHealth() < control.getMaxHealth(), "Control pig took no fall damage");
        helper.assertTrue(runner.getHealth() == runner.getMaxHealth(), "Pig wearing charged free runners took fall damage: " + runner.getHealth());
        helper.succeed();
    }

    /** EntityInvulnerabilityCheckEvent: entity types in mekanism:radiation_immune ignore radiation damage. */
    @GameTest(template = EMPTY_STRUCTURE)
    public void radiationImmuneEntitiesInvulnerable(GameTestHelper helper) {
        var golem = helper.spawn(net.minecraft.world.entity.EntityType.IRON_GOLEM, new BlockPos(1, 2, 1));
        var pig = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(3, 2, 3));
        var radiation = mekanism.common.registries.MekanismDamageTypes.RADIATION.source(helper.getLevel());
        helper.assertTrue(golem.isInvulnerableTo(radiation), "Iron golem is not immune to radiation");
        helper.assertFalse(pig.isInvulnerableTo(radiation), "Pig is immune to radiation");
        helper.succeed();
    }

    /** neoforge:swim_speed and neoforge:creative_flight exist and are on the default attribute suppliers. */
    @GameTest(template = EMPTY_STRUCTURE)
    public void neoForgeAttributesPresent(GameTestHelper helper) {
        var pig = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(1, 2, 1));
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        helper.assertTrue(pig.getAttribute(mekanism.fabric.NeoAttributes.SWIM_SPEED) != null, "Pig has no swim speed attribute");
        helper.assertTrue(player.getAttribute(mekanism.fabric.NeoAttributes.CREATIVE_FLIGHT) != null, "Player has no creative flight attribute");
        helper.succeed();
    }

    /** Forced chunks are recorded in the per-level store (saved with the world and reinstated on server start) and removed again. */
    @GameTest(template = EMPTY_STRUCTURE)
    public void chunkLoaderTicketsPersisted(GameTestHelper helper) {
        var level = helper.getLevel();
        var controller = mekanism.common.tile.component.TileComponentChunkLoader.TICKET_CONTROLLER;
        BlockPos owner = helper.absolutePos(new BlockPos(1, 1, 1));
        int chunkX = (owner.getX() >> 4) + 64;
        int chunkZ = owner.getZ() >> 4;
        var store = mekanism.neoshim.neoforge.common.world.chunk.ForcedChunkStore.get(level);
        controller.forceChunk(level, owner, chunkX, chunkZ, true, false);
        net.minecraft.nbt.CompoundTag saved = store.save(new net.minecraft.nbt.CompoundTag(), level.registryAccess());
        helper.assertTrue(saved.contains(controller.id().toString()), "Forced chunk not recorded for " + controller.id() + ": " + saved);
        controller.forceChunk(level, owner, chunkX, chunkZ, false, false);
        saved = store.save(new net.minecraft.nbt.CompoundTag(), level.registryAccess());
        helper.assertFalse(saved.contains(controller.id().toString()), "Forced chunk not removed: " + saved);
        helper.succeed();
    }

    /**
     * Position-aware light emission of a lava filled fluid tank. Propagation through the light engine is checked client side by the world smoke test (the server
     * light engine runs off thread, where vanilla returns no block entities; upstream Mekanism only rechecks tank lighting on the client too).
     */
    @GameTest(template = EMPTY_STRUCTURE)
    public void fluidTankEmitsFluidLight(GameTestHelper helper) {
        placeLikeItem(helper, MekanismBlocks.BASIC_FLUID_TANK.value());
        BlockPos absolute = helper.absolutePos(POS);
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(helper.getLevel(), absolute, Direction.UP);
        helper.assertTrue(storage != null, "No fluid storage exposed by the fluid tank");
        try (Transaction transaction = Transaction.openOuter()) {
            storage.insert(FluidVariant.of(Fluids.LAVA), FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        int emission = helper.getLevel().getBlockState(absolute).getLightEmission(helper.getLevel(), absolute);
        helper.assertTrue(emission == 15, "Positional emission of a lava tank is " + emission);
        helper.succeed();
    }

    /** Additions' custom biome modifier type (mekanismadditions:baby_mob_spawn) is decoded and replayed: plains spawn baby skeletons. */
    @GameTest(template = EMPTY_STRUCTURE)
    public void babyMobsSpawnInPlains(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        var plains = biomes.getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS);
        boolean found = plains.getMobSettings().getMobs(net.minecraft.world.entity.MobCategory.MONSTER).unwrap().stream()
              .anyMatch(spawner -> net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(spawner.type).toString().equals("mekanismadditions:baby_skeleton"));
        helper.assertTrue(found, "Plains biome does not spawn Mekanism Additions baby skeletons");
        helper.succeed();
    }
}
