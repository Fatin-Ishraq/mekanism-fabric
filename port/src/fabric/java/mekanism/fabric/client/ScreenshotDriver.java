package mekanism.fabric.client;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.energy.IEnergyContainer;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Automated screenshots for the project pages ({@code -Dmekanism_fabric.screenshots=true}, {@code gradlew runScreenshots}): builds staged scenes in a fresh flat
 * world, positions the camera, saves PNGs to {@code <run dir>/screenshots/} and quits. No human input involved.
 */
public final class ScreenshotDriver {

    private record Shot(String name, Runnable setup, int waitTicks) {
    }

    private static final List<Shot> SHOTS = new ArrayList<>();
    private static int stage = 0;
    private static int ticks = 0;
    private static int shotIndex = 0;
    private static BlockPos base;
    private static BlockPos tank;
    private static BlockPos guiPos;

    private ScreenshotDriver() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(ScreenshotDriver::tick);
    }

    private static void log(String message, Object... args) {
        Mekanism.logger.info("[Fabric port] Screenshots: " + message, args);
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(Identifier.fromNamespaceAndPath("mekanism", id));
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("mekanism", id)));
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

    /** Fills the machine's energy and puts {@code input} in the first empty slot that accepts it. */
    private static void feed(ServerLevel level, BlockPos pos, ItemStack input) {
        if (level.getBlockEntity(pos) instanceof TileEntityMekanism tile) {
            for (IEnergyContainer container : tile.getEnergyContainers(null)) {
                container.setEnergy(container.getMaxEnergy());
            }
            if (!input.isEmpty()) {
                for (IInventorySlot slot : tile.getInventorySlots(null)) {
                    if (slot.isEmpty() && slot.isItemValid(input)) {
                        slot.setStack(input);
                        break;
                    }
                }
            }
        }
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    /** Moves the (flying) player and the camera. */
    private static void camera(MinecraftServer server, double x, double y, double z, float yaw, float pitch) {
        server.execute(() -> {
            ServerPlayer player = player(server);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.teleportTo(player.serverLevel(), x, y, z, yaw, pitch);
        });
    }

    private static void buildScenes(MinecraftServer server) {
        server.execute(() -> {
            ServerPlayer player = player(server);
            ServerLevel level = player.serverLevel();
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
            level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
            level.setDayTime(6000);
            level.setWeatherParameters(6000, 0, false, false);
            base = player.blockPosition().offset(-4, 0, -6);
            //Floor
            for (int x = -3; x <= 13; x++) {
                for (int z = -8; z <= 4; z++) {
                    level.setBlockAndUpdate(base.offset(x, -1, z), Blocks.POLISHED_ANDESITE.defaultBlockState());
                }
            }
            //Front row: processing machines facing the camera, powered from a cable line behind them
            String[] front = {"enrichment_chamber", "crusher", "energized_smelter", "precision_sawmill", "metallurgic_infuser", "osmium_compressor",
                              "combiner", "purification_chamber", "chemical_injection_chamber"};
            ItemStack[] inputs = {new ItemStack(Items.IRON_ORE, 64), new ItemStack(Items.IRON_INGOT, 64), new ItemStack(Items.RAW_GOLD, 64),
                                  new ItemStack(Items.OAK_LOG, 64), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
            for (int i = 0; i < front.length; i++) {
                place(level, base.offset(i, 0, 0), facing(block(front[i]), Direction.SOUTH));
                place(level, base.offset(i, 0, -1), block("ultimate_universal_cable").defaultBlockState());
            }
            place(level, base.offset(front.length, 0, -1), facing(block("ultimate_energy_cube"), Direction.WEST));
            for (int i = 0; i < front.length; i++) {
                feed(level, base.offset(i, 0, 0), inputs[i]);
            }
            //Back row (raised): chemical machines
            String[] back = {"electrolytic_separator", "chemical_oxidizer", "chemical_infuser", "chemical_washer", "chemical_crystallizer",
                             "chemical_dissolution_chamber", "rotary_condensentrator", "elite_enriching_factory", "ultimate_enriching_factory"};
            for (int i = 0; i < back.length; i++) {
                level.setBlockAndUpdate(base.offset(i, 0, -3), Blocks.POLISHED_ANDESITE.defaultBlockState());
                place(level, base.offset(i, 1, -3), facing(block(back[i]), Direction.SOUTH));
            }
            //Transmitters between the rows
            for (int i = 0; i < back.length; i++) {
                place(level, base.offset(i, 0, -2), block(i % 3 == 0 ? "ultimate_logistical_transporter" : i % 3 == 1 ? "ultimate_mechanical_pipe"
                                                                                                                       : "ultimate_pressurized_tube").defaultBlockState());
            }
            //Dynamic tank (4x4x4: casing edges, structural glass faces, one valve), filled with water once formed
            tank = base.offset(-2, 0, -8);
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    for (int z = 0; z < 4; z++) {
                        int edges = (x == 0 || x == 3 ? 1 : 0) + (y == 0 || y == 3 ? 1 : 0) + (z == 0 || z == 3 ? 1 : 0);
                        if (edges == 0) {
                            continue;
                        }
                        String id = edges >= 2 ? "dynamic_tank" : "structural_glass";
                        if (x == 1 && y == 1 && z == 3) {
                            id = "dynamic_valve";
                        }
                        place(level, tank.offset(x, y, z), block(id).defaultBlockState());
                    }
                }
            }
            //A machine for the GUI shots, away from the scene
            guiPos = base.offset(10, 0, 3);
            place(level, guiPos, facing(block("enrichment_chamber"), Direction.SOUTH));
            feed(level, guiPos, new ItemStack(Items.IRON_ORE, 64));
            //Player gear for the MekaSuit shot
            player.setItemSlot(EquipmentSlot.HEAD, item("mekasuit_helmet"));
            player.setItemSlot(EquipmentSlot.CHEST, item("mekasuit_bodyarmor"));
            player.setItemSlot(EquipmentSlot.LEGS, item("mekasuit_pants"));
            player.setItemSlot(EquipmentSlot.FEET, item("mekasuit_boots"));
            player.setItemInHand(InteractionHand.MAIN_HAND, item("meka_tool"));
            log("scenes built at {}", base);
        });
    }

    private static void fillTank(MinecraftServer server) {
        server.execute(() -> {
            ServerLevel level = player(server).serverLevel();
            if (level.getBlockEntity(tank) instanceof TileEntityMultiblock<?> tile && tile.getMultiblock() instanceof TankMultiblockData data && data.isFormed()) {
                var fluidTank = data.mergedTank.getFluidTank();
                fluidTank.setStack(new FluidStack(Fluids.WATER, (int) Math.min(Integer.MAX_VALUE, fluidTank.getCapacity() * 3L / 4)));
                log("dynamic tank filled");
            } else {
                log("dynamic tank not formed");
            }
        });
    }

    private static void openGui(MinecraftServer server, BlockPos pos) {
        server.execute(() -> {
            ServerPlayer player = player(server);
            ServerLevel level = player.serverLevel();
            level.getBlockState(pos).useWithoutItem(level, player, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        });
    }

    private static void defineShots(Minecraft client) {
        MinecraftServer server = client.getSingleplayerServer();
        double cx = base.getX() + 4.5;
        //Machine room, from the front and slightly above
        SHOTS.add(new Shot("mekanism_machines", () -> {
            client.options.hideGui = true;
            client.options.setCameraType(CameraType.FIRST_PERSON);
            camera(server, cx, base.getY() + 3.2, base.getZ() + 6.5, 180, 22);
        }, 60));
        //Wide angle from the side, including the dynamic tank
        SHOTS.add(new Shot("mekanism_factory_overview", () -> camera(server, base.getX() + 13.5, base.getY() + 6, base.getZ() + 5, 135, 28), 60));
        //Dynamic tank close-up
        SHOTS.add(new Shot("mekanism_dynamic_tank", () -> {
            camera(server, tank.getX() + 5.5, tank.getY() + 4.5, tank.getZ() + 8.5, 145, 25);
        }, 60));
        //MekaSuit, third person from the front
        SHOTS.add(new Shot("mekanism_mekasuit", () -> {
            client.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            camera(server, cx, base.getY() + 0.0, base.getZ() + 3.5, 0, 8);
            server.execute(() -> {
                ServerPlayer player = player(server);
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            });
        }, 60));
        //GUI of a running machine
        SHOTS.add(new Shot("mekanism_gui_enrichment_chamber", () -> {
            client.options.setCameraType(CameraType.FIRST_PERSON);
            client.options.hideGui = false;
            client.getToasts().clear();
            server.execute(() -> {
                //Without the (uncharged) MekaSuit its HUD stays off in the GUI shots
                ServerPlayer player = player(server);
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND}) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                }
            });
            camera(server, guiPos.getX() + 0.5, guiPos.getY(), guiPos.getZ() + 2.5, 180, 15);
            openGui(server, guiPos);
        }, 70));
        //Creative tab
        SHOTS.add(new Shot("mekanism_creative_tab", () -> {
            if (client.player != null) {
                client.player.closeContainer();
            }
            var screen = new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(client.player, client.player.connection.enabledFeatures(), true);
            client.setScreen(screen);
            BuiltInRegistries.CREATIVE_MODE_TAB.entrySet().stream().filter(e -> e.getKey().identifier().equals(Mekanism.rl("mekanism")))
                  .findFirst().ifPresent(e -> {
                      //Modded tabs live on Fabric's extra creative tab pages
                      var fabricScreen = (net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen) screen;
                      for (int page = 0; page < fabricScreen.getPageCount(); page++) {
                          if (fabricScreen.getItemGroupsOnPage(page).contains(e.getValue())) {
                              fabricScreen.switchToPage(page);
                              break;
                          }
                      }
                      screen.selectTab(e.getValue());
                  });
        }, 30));
    }

    private static void tick(Minecraft client) {
        switch (stage) {
            case 0 -> {
                if (client.getOverlay() == null && client.screen != null && client.level == null) {
                    stage = 1;
                    client.options.pauseOnLostFocus = false;
                    //No "Move with W, A, S and D" tutorial toast in the shots
                    client.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                    log("creating world");
                    LevelSettings settings = new LevelSettings("mekanism_screenshots", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(),
                          WorldDataConfiguration.DEFAULT);
                    client.createWorldOpenFlows().createFreshLevel("mekanism_screenshots_" + System.currentTimeMillis(), settings, new WorldOptions(1L, false, false),
                          registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                          client.screen);
                }
            }
            case 1 -> {
                if (client.level != null && client.player != null && client.getSingleplayerServer() != null && ++ticks > 40) {
                    stage = 2;
                    ticks = 0;
                    buildScenes(client.getSingleplayerServer());
                }
            }
            case 2 -> {
                //Let chunks render, multiblocks form and machines start
                if (base != null && ++ticks == 100) {
                    fillTank(client.getSingleplayerServer());
                }
                if (base != null && ticks > 120) {
                    stage = 3;
                    ticks = 0;
                    defineShots(client);
                }
            }
            case 3 -> {
                if (shotIndex >= SHOTS.size()) {
                    log("done, {} screenshots", SHOTS.size());
                    stage = 4;
                    client.stop();
                    return;
                }
                Shot shot = SHOTS.get(shotIndex);
                if (ticks == 0) {
                    shot.setup().run();
                }
                if (++ticks > shot.waitTicks()) {
                    Screenshot.grab(client.gameDirectory, shot.name() + ".png", client.getMainRenderTarget(), message -> log("{}", message.getString()));
                    shotIndex++;
                    ticks = 0;
                }
            }
            default -> {
            }
        }
    }
}
