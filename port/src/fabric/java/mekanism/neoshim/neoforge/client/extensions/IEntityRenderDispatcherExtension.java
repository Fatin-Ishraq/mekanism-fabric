package mekanism.neoshim.neoforge.client.extensions;

/**
 * Injected into {@code EntityRenderDispatcher}. 26.1 moved player skin renderers to {@code PlayerModelType}-keyed maps that vanilla already
 * exposes internally, so NeoForge's {@code getSkinMap()} accessor has no remaining callers.
 */
public interface IEntityRenderDispatcherExtension {
}
