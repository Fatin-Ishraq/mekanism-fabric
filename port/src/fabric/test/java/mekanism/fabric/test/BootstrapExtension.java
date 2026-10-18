package mekanism.fabric.test;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Bootstraps vanilla before upstream's unit tests run (NeoForge's JUnit runtime does this for upstream). Registered for every test class through JUnit's
 * extension auto-detection (META-INF/services + junit-platform.properties). The tests run inside Knot (fabric-loader-junit) while JUnit loads extensions with
 * the app class loader, so the bootstrap is invoked reflectively through the test class's (Knot) class loader.
 */
public class BootstrapExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ClassLoader loader = context.getRequiredTestClass().getClassLoader();
        Class.forName("net.minecraft.SharedConstants", true, loader).getMethod("tryDetectVersion").invoke(null);
        Class.forName("net.minecraft.server.Bootstrap", true, loader).getMethod("bootStrap").invoke(null);
    }
}
