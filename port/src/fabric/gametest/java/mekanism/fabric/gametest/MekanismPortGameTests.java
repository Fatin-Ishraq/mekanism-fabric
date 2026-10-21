package mekanism.fabric.gametest;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnergyUtils;
import mekanism.neoshim.neoforge.capabilities.Capabilities;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.energy.EnergyHandler;
import mekanism.neoshim.neoforge.transfer.fluid.FluidResource;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import team.reborn.energy.api.EnergyStorage;

/**
 * Headless functional checks for the Fabric port (run with {@code gradlew runGametest}). They exercise real Mekanism block entities through
 * the Fabric Transfer API / Team Reborn Energy bridges, recipes, ticking and data maps.
 */
public class MekanismPortGameTests {

    private static final BlockPos POS = new BlockPos(1, 1, 1);

    @GameTest
    public void machineBlockEntityTicks(GameTestHelper helper) {
        helper.setBlock(POS, MekanismBlocks.ENRICHMENT_CHAMBER.value());
        BlockEntity be = helper.getBlockEntity(POS, BlockEntity.class);
        helper.assertTrue(be instanceof TileEntityMekanism, "Enrichment chamber has no Mekanism block entity: " + be);
        helper.runAfterDelay(5, helper::succeed);
    }

    /** Places a block and applies its item's default components, like placing it from an item does (side config defaults etc.). */
    private static void placeLikeItem(GameTestHelper helper, net.minecraft.world.level.block.Block block) {
        helper.setBlock(POS, block);
        BlockEntity be = helper.getBlockEntity(POS, BlockEntity.class);
        be.applyComponentsFromItemStack(new ItemStack(block));
        be.setChanged();
    }

    @GameTest(maxTicks = 400)
    public void enrichmentChamberProcessesViaTransferApi(GameTestHelper helper) {
        placeLikeItem(helper, MekanismBlocks.ENRICHMENT_CHAMBER.value());
        BlockPos absolute = helper.absolutePos(POS);
        //Energy through Team Reborn Energy (try every side; machines only accept energy on their configured input sides)
        long totalInserted = 0;
        StringBuilder report = new StringBuilder();
        for (Direction side : Direction.values()) {
            EnergyStorage energy = EnergyStorage.SIDED.find(helper.getLevel(), absolute, side);
            report.append(side).append('=').append(energy == null ? "none" : energy.getAmount() + "/" + energy.getCapacity())
                  .append(",strict=").append(mekanism.common.capabilities.Capabilities.ENERGY.block().getCapability(helper.getLevel(), absolute, null, null, side))
                  .append(",fe=").append(Capabilities.Energy.BLOCK.getCapability(helper.getLevel(), absolute, null, null, side)).append(' ');
            if (energy != null) {
                try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction =
                      net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
                    totalInserted += energy.insert(1_000_000, transaction);
                    transaction.commit();
                }
            }
        }
        report.append(" null-side strict=").append(mekanism.common.capabilities.Capabilities.ENERGY.block().getCapability(helper.getLevel(), absolute, null, null, null))
              .append(" items(up)=").append(Capabilities.Item.BLOCK.getCapability(helper.getLevel(), absolute, null, null, Direction.UP))
              .append(" be=").append(helper.getLevel().getBlockEntity(absolute))
              .append(" facing=").append(((TileEntityMekanism) helper.getLevel().getBlockEntity(absolute)).getDirection())
              .append(" energyConfig=").append(describeEnergyConfig((TileEntityMekanism) helper.getLevel().getBlockEntity(absolute)))
              .append(" canHandleEnergy=").append(((TileEntityMekanism) helper.getLevel().getBlockEntity(absolute)).canHandleEnergy());
        helper.assertTrue(totalInserted > 0, "Enrichment chamber accepted no energy: " + report);
        //Input through the Fabric item Transfer API
        Storage<ItemVariant> items = ItemStorage.SIDED.find(helper.getLevel(), absolute, Direction.UP);
        helper.assertTrue(items != null, "No item storage exposed by the enrichment chamber");
        try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction =
              net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
            long inserted = items.insert(ItemVariant.of(Items.IRON_ORE), 1, transaction);
            helper.assertTrue(inserted == 1, "Could not insert iron ore (inserted " + inserted + ")");
            transaction.commit();
        }
        helper.succeedWhen(() -> {
            ResourceHandler<ItemResource> handler = Capabilities.Item.BLOCK.getCapability(helper.getLevel(), absolute, null, null, null);
            helper.assertTrue(handler != null, "No item handler");
            boolean found = false;
            for (int slot = 0; slot < handler.size(); slot++) {
                if (handler.getResource(slot).getItem().builtInRegistryHolder().key().location().getPath().equals("dust_iron")
                      && handler.getAmountAsInt(slot) == 2) {
                    found = true;
                }
            }
            helper.assertTrue(found, "Iron ore was not enriched into 2 iron dust yet");
        });
    }

    @SuppressWarnings("unchecked")
    private static EnumProperty<Direction> facingProperty(net.minecraft.world.level.block.state.properties.Property<?> property) {
        return (EnumProperty<Direction>) property;
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

    @GameTest
    public void fluidTankAcceptsFabricFluids(GameTestHelper helper) {
        placeLikeItem(helper, MekanismBlocks.BASIC_FLUID_TANK.value());
        BlockPos absolute = helper.absolutePos(POS);
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(helper.getLevel(), absolute, Direction.UP);
        helper.assertTrue(storage != null, "No fluid storage exposed by the fluid tank");
        try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction =
              net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
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

    @GameTest
    public void vanillaChestVisibleAsItemHandler(GameTestHelper helper) {
        helper.setBlock(POS, Blocks.CHEST);
        BlockPos absolute = helper.absolutePos(POS);
        ResourceHandler<ItemResource> handler = Capabilities.Item.BLOCK.getCapability(helper.getLevel(), absolute, null, null, Direction.UP);
        helper.assertTrue(handler != null, "Vanilla chest is not visible through the NeoForge item capability bridge");
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(ItemResource.of(Items.DIAMOND), 5, transaction);
            helper.assertTrue(inserted == 5, "Chest only accepted " + inserted + " of 5 diamonds");
            transaction.commit();
        }
        helper.assertTrue(handler.getResource(0).is(Items.DIAMOND) && handler.getAmountAsInt(0) == 5, "Chest slot does not contain the diamonds");
        helper.succeed();
    }

    @GameTest
    public void chemicalDataMapsLoaded(GameTestHelper helper) {
        Holder<Chemical> hydrogen = MekanismChemicals.HYDROGEN;
        Object fuel = hydrogen.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel());
        helper.assertTrue(fuel != null, "Hydrogen has no chemical fuel data map entry");
        helper.assertTrue(MekanismAPI.CHEMICAL_REGISTRY.size() > 50, "Chemical registry looks empty");
        helper.succeed();
    }

    @GameTest(maxTicks = 300)
    public void universalCablesFormNetworkAndDeliverEnergy(GameTestHelper helper) {
        BlockPos machine = new BlockPos(1, 1, 1);
        helper.setBlock(machine, MekanismBlocks.ENRICHMENT_CHAMBER.value());
        BlockEntity machineTile = helper.getBlockEntity(machine, BlockEntity.class);
        machineTile.applyComponentsFromItemStack(new ItemStack(MekanismBlocks.ENRICHMENT_CHAMBER.value()));
        BlockPos cableA = new BlockPos(2, 1, 1);
        BlockPos cableB = new BlockPos(3, 1, 1);
        helper.setBlock(cableA, MekanismBlocks.BASIC_UNIVERSAL_CABLE.value());
        helper.setBlock(cableB, MekanismBlocks.BASIC_UNIVERSAL_CABLE.value());
        helper.runAfterDelay(5, () -> {
            var transmitter = ((mekanism.common.tile.transmitter.TileEntityTransmitter) helper.getBlockEntity(cableA, BlockEntity.class)).getTransmitter();
            helper.assertTrue(transmitter.hasTransmitterNetwork(), "Universal cable did not form a network");
            helper.assertTrue(transmitter.getTransmitterNetwork().transmittersSize() == 2,
                  "Cable network has " + transmitter.getTransmitterNetwork().transmittersSize() + " transmitters instead of 2");
            //Real energy source: a creative energy cube whose front (output by default) faces the cable network
            BlockPos cube = new BlockPos(4, 1, 1);
            BlockState cubeState = MekanismBlocks.CREATIVE_ENERGY_CUBE.value().defaultBlockState();
            for (var property : cubeState.getProperties()) {
                if (property.getName().equals("facing") && property instanceof EnumProperty) {
                    cubeState = cubeState.setValue(facingProperty(property), Direction.WEST);
                }
            }
            helper.setBlock(cube, cubeState);
            helper.getBlockEntity(cube, BlockEntity.class).applyComponentsFromItemStack(new ItemStack(MekanismBlocks.CREATIVE_ENERGY_CUBE.value()));
            //Fill it (the "filled" creative cube only exists as a creative-tab item variant)
            ((mekanism.common.tile.TileEntityEnergyCube) helper.getBlockEntity(cube, BlockEntity.class)).getEnergyContainer()
                  .setEnergy(Long.MAX_VALUE, null);
            helper.succeedWhen(() -> {
                EnergyHandler strict = mekanism.common.capabilities.Capabilities.ENERGY.block()
                      .getCapability(helper.getLevel(), helper.absolutePos(machine), null, null, null);
                if (strict == null || strict.getAmountAsLong() <= 0) {
                    EnergyHandler cubeStrict = mekanism.common.capabilities.Capabilities.ENERGY.block()
                          .getCapability(helper.getLevel(), helper.absolutePos(cube), null, null, null);
                    var cableB2 = (mekanism.common.content.network.transmitter.UniversalCable) ((mekanism.common.tile.transmitter.TileEntityTransmitter) helper.getBlockEntity(
                          cableB, BlockEntity.class)).getTransmitter();
                    var cableA2 = (mekanism.common.content.network.transmitter.UniversalCable) ((mekanism.common.tile.transmitter.TileEntityTransmitter) helper.getBlockEntity(
                          cableA, BlockEntity.class)).getTransmitter();
                    var cubeFacing = ((TileEntityMekanism) helper.getBlockEntity(cube, BlockEntity.class)).getDirection();
                    EnergyHandler cubeWest = mekanism.common.capabilities.Capabilities.ENERGY.block()
                          .getCapability(helper.getLevel(), helper.absolutePos(cube), null, null, Direction.WEST);
                    helper.fail("Energy did not reach the machine: cube=" + (cubeStrict == null ? "null" : cubeStrict.getAmountAsLong() + "/" + cubeStrict.getCapacityAsLong())
                                + " cubeFacing=" + cubeFacing + " cubeWestCap=" + cubeWest
                                + " cableB east=" + cableB2.getConnectionType(Direction.EAST) + " cableA west=" + cableA2.getConnectionType(Direction.WEST)
                                + " net energy=" + (cableB2.hasTransmitterNetwork()
                                                     ? cableB2.getTransmitterNetwork().energyContainer.getAmountAsLong()
                                                       + " acceptors=" + cableB2.getTransmitterNetwork().getAcceptorCount() : "none"));
                }
            });
        });
    }

    @GameTest(maxTicks = 200)
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
            var tile = (mekanism.common.tile.prefab.TileEntityMultiblock<?>) helper.getBlockEntity(new BlockPos(0, 1, 0), BlockEntity.class);
            helper.assertTrue(tile.getMultiblock().isFormed(), "Dynamic tank did not form");
        });
    }

    @GameTest
    public void oresAddedToOverworldBiomes(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        var plains = biomes.getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS).value();
        boolean found = plains.getGenerationSettings().features().stream()
              .flatMap(set -> set.stream())
              .anyMatch(feature -> feature.unwrapKey().map(key -> key.location().toString().equals("mekanism:ore_osmium_upper")).orElse(false));
        helper.assertTrue(found, "Plains biome does not generate Mekanism osmium ore");
        helper.succeed();
    }

    /** ItemAttributeModifierEvent: the Meka-Tool has no static attribute modifiers, its attack damage comes only from the event. */
    @GameTest
    public void mekaToolAttributesFromEvent(GameTestHelper helper) {
        ItemStack mekaTool = new ItemStack(mekanism.common.registries.MekanismItems.MEKA_TOOL.get());
        double[] attackDamage = {0};
        mekaTool.forEachModifier(net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND, (attribute, modifier, display) -> {
            if (attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
                attackDamage[0] += modifier.amount();
            }
        });
        helper.assertTrue(attackDamage[0] > 0, "Meka-Tool has no attack damage modifier (ItemAttributeModifierEvent not fired)");
        helper.succeed();
    }

    /** LivingFallEvent: charged free runners absorb fall damage, an unequipped mob takes it. */
    @GameTest
    public void freeRunnersCancelFallDamage(GameTestHelper helper) {
        var control = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(1, 2, 1));
        var runner = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(3, 2, 3));
        ItemStack freeRunners = new ItemStack(mekanism.common.registries.MekanismItems.FREE_RUNNERS.get());
        IEnergyContainer container = EnergyUtils.getEnergyContainer(
              Capabilities.Energy.ITEM.getCapability(freeRunners, ItemAccess.forStack(freeRunners)));
        helper.assertTrue(container != null, "Free runners have no energy container");
        container.setEnergy(container.getCapacityAsLong(), null);
        runner.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, freeRunners);
        control.causeFallDamage(8, 1, control.damageSources().fall());
        runner.causeFallDamage(8, 1, runner.damageSources().fall());
        helper.assertTrue(control.getHealth() < control.getMaxHealth(), "Control pig took no fall damage");
        helper.assertTrue(runner.getHealth() == runner.getMaxHealth(), "Pig wearing charged free runners took fall damage: " + runner.getHealth());
        helper.succeed();
    }

    /** EntityInvulnerabilityCheckEvent: entity types in mekanism:radiation_immune ignore radiation damage. */
    @GameTest
    public void radiationImmuneEntitiesInvulnerable(GameTestHelper helper) {
        var golem = helper.spawn(net.minecraft.world.entity.EntityType.IRON_GOLEM, new BlockPos(1, 2, 1));
        var pig = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(3, 2, 3));
        var radiation = mekanism.common.registries.MekanismDamageTypes.RADIATION.source(helper.getLevel());
        helper.assertTrue(golem.isInvulnerableTo(helper.getLevel(), radiation), "Iron golem is not immune to radiation");
        helper.assertFalse(pig.isInvulnerableTo(helper.getLevel(), radiation), "Pig is immune to radiation");
        helper.succeed();
    }

    /** neoforge:swim_speed and neoforge:creative_flight exist and are on the default attribute suppliers. */
    @GameTest
    public void neoForgeAttributesPresent(GameTestHelper helper) {
        var pig = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(1, 2, 1));
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        helper.assertTrue(pig.getAttribute(mekanism.fabric.NeoAttributes.SWIM_SPEED) != null, "Pig has no swim speed attribute");
        helper.assertTrue(player.getAttribute(mekanism.fabric.NeoAttributes.CREATIVE_FLIGHT) != null, "Player has no creative flight attribute");
        helper.succeed();
    }

    /** Forced chunks are recorded in the per-level store (saved with the world and reinstated on server start) and removed again. */
    @GameTest
    public void chunkLoaderTicketsPersisted(GameTestHelper helper) {
        var level = helper.getLevel();
        var controller = mekanism.common.tile.component.TileComponentChunkLoader.TICKET_CONTROLLER;
        BlockPos owner = helper.absolutePos(new BlockPos(1, 1, 1));
        int chunkX = (owner.getX() >> 4) + 64;
        int chunkZ = owner.getZ() >> 4;
        var store = mekanism.neoshim.neoforge.common.world.chunk.ForcedChunkStore.get(level);
        controller.forceChunk(level, owner, chunkX, chunkZ, true, false);
        CompoundTag saved = store.toCompoundTag();
        helper.assertTrue(saved.contains(controller.id().toString()), "Forced chunk not recorded for " + controller.id() + ": " + saved);
        controller.forceChunk(level, owner, chunkX, chunkZ, false, false);
        saved = store.toCompoundTag();
        helper.assertFalse(saved.contains(controller.id().toString()), "Forced chunk not removed: " + saved);
        helper.succeed();
    }

    /**
     * Position-aware light emission of a lava filled fluid tank. Propagation through the light engine is checked client side by the world smoke test (the server
     * light engine runs off thread, where vanilla returns no block entities; upstream Mekanism only rechecks tank lighting on the client too).
     */
    @GameTest
    public void fluidTankEmitsFluidLight(GameTestHelper helper) {
        placeLikeItem(helper, MekanismBlocks.BASIC_FLUID_TANK.value());
        BlockPos absolute = helper.absolutePos(POS);
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(helper.getLevel(), absolute, Direction.UP);
        helper.assertTrue(storage != null, "No fluid storage exposed by the fluid tank");
        try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction =
              net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
            storage.insert(FluidVariant.of(Fluids.LAVA), FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        int emission = helper.getLevel().getBlockState(absolute).getLightEmission(helper.getLevel(), absolute);
        helper.assertTrue(emission == 15, "Positional emission of a lava tank is " + emission);
        helper.succeed();
    }

    /** Additions' custom biome modifier type (mekanismadditions:baby_mob_spawn) is decoded and replayed: plains spawn baby skeletons. */
    @GameTest
    public void babyMobsSpawnInPlains(GameTestHelper helper) {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("mekanismadditions")) {
            helper.succeed();
            return;
        }
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        var plains = biomes.getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS).value();
        boolean found = plains.getMobSettings().getMobs(net.minecraft.world.entity.MobCategory.MONSTER).unwrap().stream()
              .anyMatch(spawner -> net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(spawner.value().type()).toString().equals("mekanismadditions:baby_skeleton"));
        helper.assertTrue(found, "Plains biome does not spawn Mekanism Additions baby skeletons");
        helper.succeed();
    }

    /** NeoForge registry aliases: the pre-chemical-merge item id resolves to its replacement. */
    @GameTest
    public void registryAliasesResolve(GameTestHelper helper) {
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(mekanism.common.Mekanism.rl("upgrade_gas"));
        helper.assertTrue(item == mekanism.common.registries.MekanismItems.CHEMICAL_UPGRADE.get(), "mekanism:upgrade_gas resolved to " + item);
        helper.succeed();
    }

    /** Item fluid capability import: vanilla buckets (Fabric fluid storage) work as resource handlers, including the container swap. */
    @GameTest
    public void vanillaBucketsAsFluidHandlers(GameTestHelper helper) {
        ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
        ResourceHandler<FluidResource> drainHandler = Capabilities.Fluid.ITEM.getCapability(waterBucket, ItemAccess.forStack(waterBucket));
        helper.assertTrue(drainHandler != null, "Water bucket has no fluid handler");
        try (Transaction transaction = Transaction.openRoot()) {
            int drained = drainHandler.extract(0, FluidResource.of(Fluids.WATER), 1000, transaction);
            helper.assertTrue(drained == 1000, "Drained " + drained + " mB instead of 1000");
            transaction.commit();
        }
        helper.assertTrue(waterBucket.is(Items.BUCKET), "Drained container is " + waterBucket);
        ItemStack emptyBucket = new ItemStack(Items.BUCKET);
        ResourceHandler<FluidResource> fillHandler = Capabilities.Fluid.ITEM.getCapability(emptyBucket, ItemAccess.forStack(emptyBucket));
        helper.assertTrue(fillHandler != null, "Empty bucket has no fluid handler");
        try (Transaction transaction = Transaction.openRoot()) {
            int filled = fillHandler.insert(FluidResource.of(Fluids.LAVA), 1000, transaction);
            helper.assertTrue(filled == 1000, "Filled " + filled + " mB instead of 1000");
            transaction.commit();
        }
        helper.assertTrue(emptyBucket.is(Items.LAVA_BUCKET), "Filled container is " + emptyBucket);
        helper.succeed();
    }

    /** Item capability export: a fluid tank item accepts fluid through FluidStorage.ITEM, an energy tablet charges through EnergyStorage.ITEM. */
    @GameTest
    public void mekanismItemsExportFabricItemStorage(GameTestHelper helper) {
        net.minecraft.world.SimpleContainer inventory = new net.minecraft.world.SimpleContainer(2);
        inventory.setItem(0, new ItemStack(MekanismBlocks.BASIC_FLUID_TANK.value()));
        inventory.setItem(1, new ItemStack(mekanism.common.registries.MekanismItems.ENERGY_TABLET.get()));
        ContainerStorage slots = ContainerStorage.of(inventory, null);
        var tankContext = net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.ofSingleSlot(slots.getSlot(0));
        Storage<FluidVariant> tank = FluidStorage.ITEM.find(inventory.getItem(0), tankContext);
        helper.assertTrue(tank != null, "Fluid tank item exposes no FluidStorage.ITEM");
        try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction =
              net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
            long inserted = tank.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, transaction);
            helper.assertTrue(inserted == FluidConstants.BUCKET, "Inserted " + inserted + " droplets into the fluid tank item");
            transaction.commit();
        }
        ResourceHandler<FluidResource> tankHandler = Capabilities.Fluid.ITEM.getCapability(inventory.getItem(0), ItemAccess.forStack(inventory.getItem(0)));
        helper.assertTrue(tankHandler != null && tankHandler.getAmountAsInt(0) == 1000, "Fluid tank item holds "
              + (tankHandler == null ? "?" : tankHandler.getAmountAsInt(0)) + " mB after the exchange");
        var tabletContext = net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.ofSingleSlot(slots.getSlot(1));
        EnergyStorage tablet = EnergyStorage.ITEM.find(inventory.getItem(1), tabletContext);
        helper.assertTrue(tablet != null, "Energy tablet exposes no EnergyStorage.ITEM");
        try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction =
              net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
            long inserted = tablet.insert(10_000, transaction);
            helper.assertTrue(inserted > 0, "Energy tablet accepted no energy");
            transaction.commit();
        }
        IEnergyContainer container = EnergyUtils.getEnergyContainer(
              Capabilities.Energy.ITEM.getCapability(inventory.getItem(1), ItemAccess.forStack(inventory.getItem(1))));
        helper.assertTrue(container != null && container.getAmountAsLong() > 0, "Energy tablet has no energy after the exchange");
        helper.succeed();
    }
}
