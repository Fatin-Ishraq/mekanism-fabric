package mekanism.neoshim.neoforge.fluids;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import mekanism.neoshim.neoforge.common.SoundAction;
import mekanism.neoshim.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

/**
 * Per-fluid physical properties. Vanilla and foreign Fabric fluids get a FluidType derived from Fabric's {@link FluidVariantAttributes};
 * fluids extending {@link BaseFlowingFluid} carry their own.
 */
public class FluidType {

    public static final int BUCKET_VOLUME = 1000;
    public static final Map<Fluid, FluidType> DERIVED_TYPES = new IdentityHashMap<>();

    private final String descriptionId;
    private final double motionScale;
    private final boolean canPushEntity;
    private final boolean canSwim;
    private final boolean canDrown;
    private final float fallDistanceModifier;
    private final boolean canExtinguish;
    private final boolean canConvertToSource;
    private final boolean supportsBoating;
    @Nullable
    private final PathType pathType;
    @Nullable
    private final PathType adjacentPathType;
    private final Map<SoundAction, SoundEvent> sounds;
    private final boolean canHydrate;
    private final int lightLevel;
    private final int density;
    private final int temperature;
    private final int viscosity;
    private final Rarity rarity;
    private Object renderProperties;

    public FluidType(Properties properties) {
        this.descriptionId = properties.descriptionId;
        this.motionScale = properties.motionScale;
        this.canPushEntity = properties.canPushEntity;
        this.canSwim = properties.canSwim;
        this.canDrown = properties.canDrown;
        this.fallDistanceModifier = properties.fallDistanceModifier;
        this.canExtinguish = properties.canExtinguish;
        this.canConvertToSource = properties.canConvertToSource;
        this.supportsBoating = properties.supportsBoating;
        this.pathType = properties.pathType;
        this.adjacentPathType = properties.adjacentPathType;
        this.sounds = Map.copyOf(properties.sounds);
        this.canHydrate = properties.canHydrate;
        this.lightLevel = properties.lightLevel;
        this.density = properties.density;
        this.temperature = properties.temperature;
        this.viscosity = properties.viscosity;
        this.rarity = properties.rarity;
    }

    /** Fluid type lookup used by the injected {@code Fluid#getFluidType()} for fluids that do not define their own. */
    public static FluidType of(Fluid fluid) {
        if (fluid instanceof BaseFlowingFluid flowing) {
            return flowing.getFluidType();
        }
        synchronized (DERIVED_TYPES) {
            return DERIVED_TYPES.computeIfAbsent(fluid, FluidType::derive);
        }
    }

    private static FluidType derive(Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return VanillaTypes.EMPTY;
        } else if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return VanillaTypes.WATER;
        } else if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return VanillaTypes.LAVA;
        }
        FluidVariant variant = FluidVariant.of(fluid);
        Properties properties = Properties.create()
              .descriptionId(Util.makeDescriptionId("fluid", BuiltInRegistries.FLUID.getKey(fluid)))
              .lightLevel(FluidVariantAttributes.getLuminance(variant))
              .temperature(FluidVariantAttributes.getTemperature(variant))
              .viscosity(FluidVariantAttributes.getViscosity(variant, null))
              .density(FluidVariantAttributes.isLighterThanAir(variant) ? -1000 : 1000);
        return new FluidType(properties) {
            @Override
            public Component getDescription() {
                return FluidVariantAttributes.getName(variant);
            }
        };
    }

    public Component getDescription() {
        return Component.translatable(this.getDescriptionId());
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }

    public Component getDescription(FluidStack stack) {
        return getDescription();
    }

    public String getDescriptionId(FluidStack stack) {
        return this.getDescriptionId();
    }

    public double motionScale(net.minecraft.world.entity.Entity entity) {
        return this.motionScale;
    }

    public boolean canPushEntity(net.minecraft.world.entity.Entity entity) {
        return this.canPushEntity;
    }

    public boolean canSwim(net.minecraft.world.entity.Entity entity) {
        return this.canSwim;
    }

    public float getFallDistanceModifier(net.minecraft.world.entity.Entity entity) {
        return this.fallDistanceModifier;
    }

    public boolean canExtinguish(net.minecraft.world.entity.Entity entity) {
        return this.canExtinguish;
    }

    public boolean canDrownIn(net.minecraft.world.entity.LivingEntity entity) {
        return this.canDrown;
    }

    public boolean supportsBoating(net.minecraft.world.entity.vehicle.Boat boat) {
        return this.supportsBoating;
    }

    public boolean canConvertToSource(FluidState state, net.minecraft.world.level.LevelReader reader, BlockPos pos) {
        return this.canConvertToSource;
    }

    public boolean canConvertToSource(FluidStack stack) {
        return this.canConvertToSource;
    }

    @Nullable
    public PathType getBlockPathType(FluidState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
          @Nullable net.minecraft.world.entity.Mob mob, boolean canFluidLog) {
        return canFluidLog ? this.pathType : null;
    }

    @Nullable
    public PathType getAdjacentBlockPathType(FluidState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
          @Nullable net.minecraft.world.entity.Mob mob, PathType originalType) {
        return this.adjacentPathType;
    }

    @Nullable
    public SoundEvent getSound(SoundAction action) {
        return this.sounds.get(action);
    }

    @Nullable
    public SoundEvent getSound(@Nullable net.minecraft.world.entity.player.Player player, net.minecraft.world.level.BlockGetter getter, BlockPos pos,
          SoundAction action) {
        return getSound(action);
    }

    @Nullable
    public SoundEvent getSound(FluidStack stack, SoundAction action) {
        return getSound(action);
    }

    public boolean canHydrate(net.minecraft.world.entity.Entity entity) {
        return this.canHydrate;
    }

    public boolean canHydrate(FluidStack stack) {
        return this.canHydrate;
    }

    public int getLightLevel() {
        return this.lightLevel;
    }

    public int getLightLevel(FluidStack stack) {
        return getLightLevel();
    }

    public int getLightLevel(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getLightLevel();
    }

    public int getDensity() {
        return this.density;
    }

    public int getDensity(FluidStack stack) {
        return getDensity();
    }

    public int getDensity(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getDensity();
    }

    public int getTemperature() {
        return this.temperature;
    }

    public int getTemperature(FluidStack stack) {
        return getTemperature();
    }

    public int getTemperature(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getTemperature();
    }

    public int getViscosity() {
        return this.viscosity;
    }

    public int getViscosity(FluidStack stack) {
        return getViscosity();
    }

    public int getViscosity(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getViscosity();
    }

    public Rarity getRarity() {
        return this.rarity;
    }

    public Rarity getRarity(FluidStack stack) {
        return getRarity();
    }

    public boolean isLighterThanAir() {
        return this.density <= 0;
    }

    public boolean isAir() {
        return this == VanillaTypes.EMPTY;
    }

    public boolean isVanilla() {
        return this == VanillaTypes.EMPTY || this == VanillaTypes.WATER || this == VanillaTypes.LAVA;
    }

    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        return level.dimensionType().ultraWarm() && this == VanillaTypes.WATER;
    }

    public void onVaporize(@Nullable net.minecraft.world.entity.player.Player player, Level level, BlockPos pos, FluidStack stack) {
        SoundEvent sound = this.getSound(player, level, pos, mekanism.neoshim.neoforge.common.SoundActions.FLUID_VAPORIZE);
        level.playSound(player, pos, sound != null ? sound : net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F,
              2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        for (int i = 0; i < 8; ++i) {
            level.addAlwaysVisibleParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(),
                  pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
        }
    }

    public boolean canBePlacedInLevel(BlockAndTintGetter level, BlockPos pos, FluidState state) {
        return state.getType() != Fluids.EMPTY;
    }

    public boolean canBePlacedInLevel(BlockAndTintGetter level, BlockPos pos, FluidStack stack) {
        return !stack.isEmpty() && !stack.getFluid().defaultFluidState().createLegacyBlock().isAir();
    }

    public FluidState getStateForPlacement(BlockAndTintGetter level, BlockPos pos, FluidStack stack) {
        return stack.getFluid().defaultFluidState();
    }

    public ItemStack getBucket(FluidStack stack) {
        return new ItemStack(stack.getFluid().getBucket());
    }

    /** NeoForge's deprecated client hook. Called once by the client bootstrap to collect render properties. */
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    }

    public Object getRenderPropertiesInternal() {
        return renderProperties;
    }

    public void setRenderPropertiesInternal(Object renderProperties) {
        this.renderProperties = renderProperties;
    }

    @Override
    public String toString() {
        return "FluidType[" + descriptionId + "]";
    }

    /** Vanilla fluid types (NeoForgeMod.EMPTY_TYPE/WATER_TYPE/LAVA_TYPE). */
    public static final class VanillaTypes {

        public static final FluidType EMPTY = new FluidType(Properties.create().descriptionId("block.minecraft.air").motionScale(1).canPushEntity(false)
              .canSwim(false).canDrown(false).fallDistanceModifier(1F).pathType(null).adjacentPathType(null).density(0).temperature(0).viscosity(0)) {
        };
        public static final FluidType WATER = new FluidType(Properties.create().descriptionId("block.minecraft.water").fallDistanceModifier(0F)
              .canExtinguish(true).canConvertToSource(true).supportsBoating(true)
              .sound(mekanism.neoshim.neoforge.common.SoundActions.BUCKET_FILL, net.minecraft.sounds.SoundEvents.BUCKET_FILL)
              .sound(mekanism.neoshim.neoforge.common.SoundActions.BUCKET_EMPTY, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY)
              .sound(mekanism.neoshim.neoforge.common.SoundActions.FLUID_VAPORIZE, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH).canHydrate(true));
        public static final FluidType LAVA = new FluidType(Properties.create().descriptionId("block.minecraft.lava").canSwim(false).canDrown(false)
              .pathType(PathType.LAVA).adjacentPathType(null)
              .sound(mekanism.neoshim.neoforge.common.SoundActions.BUCKET_FILL, net.minecraft.sounds.SoundEvents.BUCKET_FILL_LAVA)
              .sound(mekanism.neoshim.neoforge.common.SoundActions.BUCKET_EMPTY, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY_LAVA)
              .lightLevel(15).density(3000).viscosity(6000).temperature(1300));

        private VanillaTypes() {
        }
    }

    public static final class Properties {

        private String descriptionId;
        private double motionScale = 0.014D;
        private boolean canPushEntity = true;
        private boolean canSwim = true;
        private boolean canDrown = true;
        private float fallDistanceModifier = 0.5F;
        private boolean canExtinguish = false;
        private boolean canConvertToSource = false;
        private boolean supportsBoating = false;
        @Nullable
        private PathType pathType = PathType.WATER;
        @Nullable
        private PathType adjacentPathType = PathType.WATER_BORDER;
        private final Map<SoundAction, SoundEvent> sounds = new HashMap<>();
        private boolean canHydrate = false;
        private int lightLevel = 0;
        private int density = 1000;
        private int temperature = 300;
        private int viscosity = 1000;
        private Rarity rarity = Rarity.COMMON;

        private Properties() {
        }

        public static Properties create() {
            return new Properties();
        }

        public Properties descriptionId(String descriptionId) {
            this.descriptionId = descriptionId;
            return this;
        }

        public Properties motionScale(double motionScale) {
            this.motionScale = motionScale;
            return this;
        }

        public Properties canPushEntity(boolean canPushEntity) {
            this.canPushEntity = canPushEntity;
            return this;
        }

        public Properties canSwim(boolean canSwim) {
            this.canSwim = canSwim;
            return this;
        }

        public Properties canDrown(boolean canDrown) {
            this.canDrown = canDrown;
            return this;
        }

        public Properties fallDistanceModifier(float fallDistanceModifier) {
            this.fallDistanceModifier = fallDistanceModifier;
            return this;
        }

        public Properties canExtinguish(boolean canExtinguish) {
            this.canExtinguish = canExtinguish;
            return this;
        }

        public Properties canConvertToSource(boolean canConvertToSource) {
            this.canConvertToSource = canConvertToSource;
            return this;
        }

        public Properties supportsBoating(boolean supportsBoating) {
            this.supportsBoating = supportsBoating;
            return this;
        }

        public Properties pathType(@Nullable PathType pathType) {
            this.pathType = pathType;
            return this;
        }

        public Properties adjacentPathType(@Nullable PathType adjacentPathType) {
            this.adjacentPathType = adjacentPathType;
            return this;
        }

        public Properties sound(SoundAction action, SoundEvent sound) {
            this.sounds.put(action, sound);
            return this;
        }

        public Properties canHydrate(boolean canHydrate) {
            this.canHydrate = canHydrate;
            return this;
        }

        public Properties lightLevel(int lightLevel) {
            this.lightLevel = lightLevel;
            return this;
        }

        public Properties density(int density) {
            this.density = density;
            return this;
        }

        public Properties temperature(int temperature) {
            this.temperature = temperature;
            return this;
        }

        public Properties viscosity(int viscosity) {
            this.viscosity = viscosity;
            return this;
        }

        public Properties rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }
    }
}
