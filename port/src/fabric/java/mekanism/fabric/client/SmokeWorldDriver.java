package mekanism.fabric.client;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.base.TileEntityMekanism;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Dev-only automated world check ({@code gradlew runSmokeWorld}, flag {@code mekanism_fabric.smokeWorld}): creates a flat creative world,
 * places a small Mekanism setup server-side (energy cube -> cables -> machine, fluid tank), checks the client received the block entities,
 * opens a machine GUI programmatically and checks the Mekanism screen is shown, then quits. No player input is involved; results are logged
 * with the "[Fabric port] World smoke" prefix.
 */
public final class SmokeWorldDriver {

    private static int stage = 0;
    private static int ticks = 0;
    private static BlockPos base;
    private static final List<String> failures = new ArrayList<>();

    private SmokeWorldDriver() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(SmokeWorldDriver::tick);
    }

    private static void log(String message, Object... args) {
        Mekanism.logger.info("[Fabric port] World smoke: " + message, args);
    }

    private static void fail(String message) {
        failures.add(message);
        Mekanism.logger.error("[Fabric port] World smoke FAIL: {}", message);
    }

    private static void tick(Minecraft client) {
        switch (stage) {
            case 0 -> {
                if (client.getOverlay() == null && client.screen != null && client.level == null) {
                    stage = 1;
                    log("creating world");
                    LevelSettings settings = new LevelSettings("mekanism_smoke", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(),
                          WorldDataConfiguration.DEFAULT);
                    client.createWorldOpenFlows().createFreshLevel("mekanism_smoke_" + System.currentTimeMillis(), settings, new WorldOptions(1L, false, false),
                          registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                          client.screen);
                }
            }
            case 1 -> {
                if (client.level != null && client.player != null && client.getSingleplayerServer() != null) {
                    if (++ticks > 40) {
                        stage = 2;
                        ticks = 0;
                        log("in world, placing setup");
                        placeSetup(client.getSingleplayerServer());
                    }
                }
            }
            case 2 -> {
                if (++ticks > 80) {
                    stage = 3;
                    ticks = 0;
                    checkClientSync(client);
                    checkServerState(client.getSingleplayerServer());
                    log("opening enrichment chamber GUI");
                    openGui(client.getSingleplayerServer());
                }
            }
            case 3 -> {
                if (++ticks > 40) {
                    stage = 4;
                    ticks = 0;
                    if (client.screen == null || !isMekanismScreen(client.screen)) {
                        fail("expected a Mekanism GUI, got " + (client.screen == null ? "no screen" : client.screen.getClass().getName()));
                    } else {
                        log("GUI open: {}", client.screen.getClass().getSimpleName());
                    }
                    if (client.player != null) {
                        client.player.closeContainer();
                    }
                    guiBlocks = collectGuiBlocks();
                    log("cycling through {} GUI blocks", guiBlocks.size());
                }
            }
            case 4 -> cycleGuiBlocks(client);
            case 5 -> {
                if (ticks == 0) {
                    tabs = net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.entrySet().stream()
                          .filter(e -> e.getKey().location().getNamespace().startsWith("mekanism")).map(java.util.Map.Entry::getValue).toList();
                    log("rendering {} Mekanism creative tabs", tabs.size());
                    var screen = new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(client.player, client.player.connection.enabledFeatures(), true);
                    client.setScreen(screen);
                }
                int tab = ticks / 10;
                if (tab < tabs.size() && ticks % 10 == 0 && client.screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen screen) {
                    screen.selectTab(tabs.get(tab));
                }
                ticks++;
                if (tab >= tabs.size()) {
                    client.setScreen(null);
                    stage = 6;
                    ticks = 0;
                }
            }
            case 6 -> {
                if (++ticks > 20) {
                    stage = 7;
                    log("finished with {} failure(s){}", failures.size(), failures.isEmpty() ? "" : ": " + failures);
                    client.stop();
                }
            }
            default -> {
            }
        }
    }

    private static List<net.minecraft.world.item.CreativeModeTab> tabs = List.of();
    private static List<Block> guiBlocks = List.of();
    private static int guiIndex = 0;
    private static final List<String> noGui = new ArrayList<>();
    private static int guiOk = 0;
    private static volatile String lastUseResult = "";

    private static List<Block> collectGuiBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (Block block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            if (net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getNamespace().startsWith("mekanism")
                && mekanism.common.block.attribute.Attribute.has(block, mekanism.common.block.attribute.AttributeGui.class)) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    private static void cycleGuiBlocks(Minecraft client) {
        ticks++;
        if (guiIndex >= guiBlocks.size()) {
            log("GUI blocks: {} opened a Mekanism screen, {} did not (multiblock parts / conditional GUIs): {}", guiOk, noGui.size(), noGui);
            stage = 5;
            ticks = 0;
            return;
        }
        Block block = guiBlocks.get(guiIndex);
        if (ticks == 1) {
            MinecraftServer server = client.getSingleplayerServer();
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                ServerLevel level = player.serverLevel();
                BlockPos pos = base.above(3);
                level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                place(level, pos, block.defaultBlockState());
            });
        } else if (ticks == 4) {
            //Open in a later tick so the client already has the block entity (like a real player interaction)
            BlockPos checkPos = base.above(3);
            if (client.level.getBlockEntity(checkPos) == null) {
                String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
                noGui.add(id + "(no client BE: " + client.level.getBlockState(checkPos) + ")");
                ticks = 17;
                return;
            }
            MinecraftServer server = client.getSingleplayerServer();
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                ServerLevel level = player.serverLevel();
                BlockPos pos = base.above(3);
                InteractionResult result = level.getBlockState(pos).useWithoutItem(level, player, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
                lastUseResult = result + " state=" + level.getBlockState(pos) + " be=" + level.getBlockEntity(pos);
            });
        } else if (ticks == 16) {
            String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
            if (client.screen != null && isMekanismScreen(client.screen)) {
                guiOk++;
            } else {
                noGui.add(id + "[" + lastUseResult + "]");
            }
            if (client.player != null) {
                client.player.closeContainer();
            }
        } else if (ticks > 18) {
            guiIndex++;
            ticks = 0;
        }
    }

    private static boolean isMekanismScreen(net.minecraft.client.gui.screens.Screen screen) {
        String name = screen.getClass().getName();
        return name.startsWith("mekanism.") && name.contains(".client.gui.");
    }

    private static BlockState facing(Block block, Direction direction) {
        BlockState state = block.defaultBlockState();
        for (var property : state.getProperties()) {
            if (property instanceof DirectionProperty directionProperty && property.getName().equals("facing") && directionProperty.getPossibleValues().contains(direction)) {
                state = state.setValue(directionProperty, direction);
            }
        }
        return state;
    }

    private static void place(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos, state);
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            be.applyComponentsFromItemStack(new ItemStack(state.getBlock()));
            be.setChanged();
        }
    }

    private static void placeSetup(MinecraftServer server) {
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            ServerLevel level = player.serverLevel();
            base = player.blockPosition().offset(3, 0, 0);
            place(level, base, MekanismBlocks.ENRICHMENT_CHAMBER.value().defaultBlockState());
            place(level, base.east(), MekanismBlocks.BASIC_UNIVERSAL_CABLE.value().defaultBlockState());
            place(level, base.east(2), MekanismBlocks.BASIC_UNIVERSAL_CABLE.value().defaultBlockState());
            place(level, base.east(3), facing(MekanismBlocks.CREATIVE_ENERGY_CUBE.value(), Direction.WEST));
            if (level.getBlockEntity(base.east(3)) instanceof TileEntityEnergyCube cube) {
                cube.getEnergyContainer().setEnergy(Long.MAX_VALUE);
            }
            place(level, base.south(2), MekanismBlocks.BASIC_FLUID_TANK.value().defaultBlockState());
            place(level, base.south(2).east(), MekanismBlocks.BASIC_ENERGY_CUBE.value().defaultBlockState());
            place(level, base.north(2), MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.value().defaultBlockState());
            place(level, base.north(2).east(), MekanismBlocks.BASIC_MECHANICAL_PIPE.value().defaultBlockState());
            place(level, base.north(2).east(2), MekanismBlocks.BASIC_PRESSURIZED_TUBE.value().defaultBlockState());
            place(level, base.north(2).east(3), MekanismBlocks.STEEL_CASING.value().defaultBlockState());
        });
    }

    private static void checkClientSync(Minecraft client) {
        if (client.level == null) {
            fail("client level vanished");
            return;
        }
        for (BlockPos pos : List.of(base, base.east(), base.east(3), base.south(2), base.north(2))) {
            BlockEntity be = client.level.getBlockEntity(pos);
            if (be == null) {
                fail("client has no block entity at " + pos + " (" + client.level.getBlockState(pos) + ")");
            } else {
                log("client BE at {}: {}", pos, be.getClass().getSimpleName());
            }
        }
        if (client.level.getBlockEntity(base) instanceof TileEntityMekanism tile && tile.getEnergyContainer(0, null) != null) {
            log("client-side chamber energy {}", tile.getEnergyContainer(0, null).getEnergy());
        }
    }

    private static void checkServerState(MinecraftServer server) {
        server.executeBlocking(() -> {
            ServerLevel level = server.overworld();
            if (level.getBlockEntity(base) instanceof TileEntityMekanism tile && tile.getEnergyContainer(0, null) != null) {
                long energy = tile.getEnergyContainer(0, null).getEnergy();
                if (energy <= 0) {
                    fail("enrichment chamber received no energy from the creative cube through the cables");
                } else {
                    log("server-side chamber energy {}", energy);
                }
            } else {
                fail("no enrichment chamber tile on the server");
            }
        });
    }

    private static void openGui(MinecraftServer server) {
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            ServerLevel level = player.serverLevel();
            BlockState state = level.getBlockState(base);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(base), Direction.UP, base, false);
            InteractionResult result = state.useWithoutItem(level, player, hit);
            log("useWithoutItem on enrichment chamber -> {}", result);
        });
    }
}
