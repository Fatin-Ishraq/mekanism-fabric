package mekanism.neoshim.neoforgespi.language;

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.Type;

/**
 * Annotation scan data. On Fabric this is produced at build time by the {@code generateScanData} Gradle task (ASM scan of our compiled
 * classes) and loaded by {@link mekanism.fabric.scan.ScanDataLoader}.
 */
public class ModFileScanData {

    private final Set<AnnotationData> annotations;
    private final List<IModFileInfo> modInfoData;

    public ModFileScanData(Set<AnnotationData> annotations, List<IModFileInfo> modInfoData) {
        this.annotations = annotations;
        this.modInfoData = modInfoData;
    }

    public Set<AnnotationData> getAnnotations() {
        return annotations;
    }

    public List<IModFileInfo> getIModInfoData() {
        return modInfoData;
    }

    public record AnnotationData(Type annotationType, ElementType targetType, Type clazz, String memberName, Map<String, Object> annotationData) {
    }
}
