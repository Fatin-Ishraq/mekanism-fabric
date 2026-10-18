package mekanism.fabric.scan;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import mekanism.common.Mekanism;
import mekanism.fabric.MekanismFabric;
import mekanism.neoshim.fml.loading.modscan.ModAnnotation;
import mekanism.neoshim.neoforgespi.language.IModFileInfo;
import mekanism.neoshim.neoforgespi.language.IModInfo;
import mekanism.neoshim.neoforgespi.language.ModFileScanData;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Replacement for FML's mod file annotation scan: walks the classes of our own jar with ASM (once, lazily) and records every annotation in
 * the {@code mekanism} packages in FML's {@link ModFileScanData.AnnotationData} format. Used by {@code MekAnnotationScanner}.
 */
public final class ScanDataLoader {

    private static List<ModFileScanData> cache;

    private ScanDataLoader() {
    }

    public static synchronized List<ModFileScanData> getAllScanData() {
        if (cache == null) {
            cache = List.of(scan());
        }
        return cache;
    }

    private static ModFileScanData scan() {
        Set<ModFileScanData.AnnotationData> annotations = new HashSet<>();
        ModContainer container = FabricLoader.getInstance().getModContainer(MekanismFabric.FABRIC_MOD_ID).orElseThrow();
        for (Path root : container.getRootPaths()) {
            Path base = root.resolve("mekanism");
            if (!Files.isDirectory(base)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(base)) {
                paths.filter(p -> p.toString().endsWith(".class")).forEach(path -> {
                    try (InputStream stream = Files.newInputStream(path)) {
                        new ClassReader(stream).accept(new Collector(annotations), ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
                    } catch (IOException e) {
                        Mekanism.logger.error("Failed to scan {}", path, e);
                    }
                });
            } catch (IOException e) {
                Mekanism.logger.error("Failed to walk {}", base, e);
            }
        }
        IModInfo modInfo = new IModInfo(Mekanism.MODID, MekanismFabric.FABRIC_MOD_ID);
        IModFileInfo fileInfo = () -> List.of(modInfo);
        return new ModFileScanData(annotations, List.of(fileInfo));
    }

    private static final class Collector extends ClassVisitor {

        private final Set<ModFileScanData.AnnotationData> annotations;
        private Type classType;

        Collector(Set<ModFileScanData.AnnotationData> annotations) {
            super(Opcodes.ASM9);
            this.annotations = annotations;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            classType = Type.getObjectType(name);
        }

        private AnnotationVisitor collect(String descriptor, ElementType target, String member) {
            Map<String, Object> values = new HashMap<>();
            Type annotationType = Type.getType(descriptor);
            return new ValueCollector(values, () -> annotations.add(new ModFileScanData.AnnotationData(annotationType, target, classType, member, values)));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            return collect(descriptor, ElementType.TYPE, classType.getClassName());
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            return new FieldVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String annotationDescriptor, boolean visible) {
                    return collect(annotationDescriptor, ElementType.FIELD, name);
                }
            };
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String annotationDescriptor, boolean visible) {
                    return collect(annotationDescriptor, ElementType.METHOD, name + descriptor);
                }
            };
        }
    }

    private static final class ValueCollector extends AnnotationVisitor {

        private final Map<String, Object> values;
        private final Runnable onEnd;

        ValueCollector(Map<String, Object> values, Runnable onEnd) {
            super(Opcodes.ASM9);
            this.values = values;
            this.onEnd = onEnd;
        }

        @Override
        public void visit(String name, Object value) {
            values.put(name, value);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            values.put(name, new ModAnnotation.EnumHolder(descriptor, value));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            Map<String, Object> nested = new HashMap<>();
            values.put(name, nested);
            return new ValueCollector(nested, () -> {
            });
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            List<Object> list = new ArrayList<>();
            values.put(name, list);
            return new AnnotationVisitor(Opcodes.ASM9) {
                @Override
                public void visit(String n, Object value) {
                    list.add(value);
                }

                @Override
                public void visitEnum(String n, String descriptor, String value) {
                    list.add(new ModAnnotation.EnumHolder(descriptor, value));
                }

                @Override
                public AnnotationVisitor visitAnnotation(String n, String descriptor) {
                    Map<String, Object> nested = new HashMap<>();
                    list.add(nested);
                    return new ValueCollector(nested, () -> {
                    });
                }
            };
        }

        @Override
        public void visitEnd() {
            onEnd.run();
        }
    }
}
