package mekanism.common.integration.framedblocks;

import io.github.xfacthd.framedblocks.api.camo.CamoContainerFactory;
import io.github.xfacthd.framedblocks.api.util.FramedConstants;
import mekanism.common.Mekanism;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.loading.FMLEnvironment;
import mekanism.neoshim.neoforge.client.event.RegisterParticleProvidersEvent;
import mekanism.neoshim.neoforge.registries.DeferredHolder;
import mekanism.neoshim.neoforge.registries.DeferredRegister;

public final class FramedBlocksIntegration {

    private static final DeferredRegister<CamoContainerFactory<?>> CAMO_FACTORIES = DeferredRegister.create(
            FramedConstants.Registries.CAMO_CONTAINER_FACTORY_REGISTRY_KEY,
            Mekanism.MODID
    );
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Mekanism.MODID);

    static final DeferredHolder<CamoContainerFactory<?>, ChemicalCamoContainerFactory> CHEMICAL_FACTORY =
            CAMO_FACTORIES.register("chemical", ChemicalCamoContainerFactory::new);
    static final DeferredHolder<ParticleType<?>, ChemicalParticleOptions.Type> CHEMICAL_PARTICLE =
            PARTICLE_TYPES.register("chemical", ChemicalParticleOptions.Type::new);

    public static void init(IEventBus modBus) {
        CAMO_FACTORIES.register(modBus);
        PARTICLE_TYPES.register(modBus);

        if (FMLEnvironment.getDist().isClient()) {
            ClientEvents.init(modBus);
        }
    }

    static final class ClientEvents {

        static void init(IEventBus modBus) {
            modBus.addListener(ClientEvents::onRegisterParticleProviders);
        }

        private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpecial(CHEMICAL_PARTICLE.get(), new ChemicalSpriteParticle.Provider());
        }
    }
}
