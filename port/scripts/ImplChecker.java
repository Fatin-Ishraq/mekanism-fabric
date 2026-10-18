import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Finds interface methods that concrete classes in the given mod jars do not implement once remapped, the classic Fabric remapping trap: a mod interface
 * declares a method (e.g. {@code Level getLevel()}) that is only "implemented" by an inherited Minecraft method, which gets renamed to its intermediary name.
 * Usage: java -cp asm.jar ImplChecker.java [--fail] mod1.jar,mod2.jar lib1.jar,lib2.jar (--fail: exit 1 if any gap is found)
 */
public class ImplChecker {

    record Info(String name, String superName, String[] interfaces, boolean isInterface, boolean isAbstract, Map<String, Integer> methods) {
    }

    static final Map<String, Info> CLASSES = new HashMap<>();
    /** owner.name+desc -> referencing class, for method references made by mod classes */
    static final Map<String, String> REFERENCES = new HashMap<>();
    static boolean collectReferences;

    static Info read(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        Info[] out = new Info[1];
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            final Map<String, Integer> methods = new HashMap<>();

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                out[0] = new Info(name, superName, interfaces == null ? new String[0] : interfaces, (access & Opcodes.ACC_INTERFACE) != 0,
                      (access & Opcodes.ACC_ABSTRACT) != 0, methods);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                methods.put(name + descriptor, access);
                if (!collectReferences) {
                    return null;
                }
                String self = out[0].name();
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String mName, String mDesc, boolean isInterface) {
                        if (!owner.startsWith("[")) {
                            REFERENCES.putIfAbsent(owner + "." + mName + mDesc, self);
                        }
                    }
                };
            }
        }, collectReferences ? ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES : ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return out[0];
    }

    static void load(String jar, List<String> collect) throws Exception {
        try (ZipFile zip = new ZipFile(jar)) {
            for (ZipEntry entry : java.util.Collections.list(zip.entries())) {
                if (entry.getName().endsWith(".class") && !entry.getName().startsWith("META-INF")) {
                    try (InputStream in = zip.getInputStream(entry)) {
                        Info info = read(in.readAllBytes());
                        CLASSES.putIfAbsent(info.name(), info);
                        if (collect != null) {
                            collect.add(info.name());
                        }
                    }
                }
            }
        }
    }

    static Info get(String name) {
        if (name == null) {
            return null;
        }
        return CLASSES.computeIfAbsent(name, n -> {
            try (InputStream in = ClassLoader.getSystemResourceAsStream(n + ".class")) {
                return in == null ? null : read(in.readAllBytes());
            } catch (Exception e) {
                return null;
            }
        });
    }

    static boolean resolves(String owner, String member, Set<String> seen) {
        if (owner == null || !seen.add(owner)) {
            return false;
        }
        Info info = get(owner);
        if (info == null) {
            //Unknown library class in the hierarchy: assume it resolves there
            return true;
        }
        if (info.methods().containsKey(member)) {
            return true;
        }
        //Signature polymorphic and java.lang.Object methods on interfaces
        if (resolves(info.superName(), member, seen)) {
            return true;
        }
        for (String iface : info.interfaces()) {
            if (resolves(iface, member, seen)) {
                return true;
            }
        }
        return info.isInterface() && resolves("java/lang/Object", member, seen);
    }

    public static void main(String[] args) throws Exception {
        boolean fail = args[0].equals("--fail");
        if (fail) {
            args = java.util.Arrays.copyOfRange(args, 1, args.length);
        }
        List<String> modClasses = new ArrayList<>();
        collectReferences = true;
        for (String jar : args[0].split(",")) {
            load(jar, modClasses);
        }
        collectReferences = false;
        for (String jar : args[1].split(",")) {
            load(jar, null);
        }
        //Interfaces injected into Minecraft classes (Loom interface injection; mixins add them at runtime)
        java.util.regex.Pattern entry = java.util.regex.Pattern.compile("\"(net/minecraft/[^\"]+)\"\\s*:\\s*\\[([^\\]]*)\\]");
        for (String jar : (args[0] + "," + args[1]).split(",")) {
            try (ZipFile zip = new ZipFile(jar)) {
                ZipEntry modJson = zip.getEntry("fabric.mod.json");
                if (modJson == null) {
                    continue;
                }
                String json = new String(zip.getInputStream(modJson).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                java.util.regex.Matcher matcher = entry.matcher(json);
                while (matcher.find()) {
                    Info target = get(matcher.group(1));
                    if (target != null) {
                        List<String> added = new ArrayList<>(List.of(target.interfaces()));
                        for (String iface : matcher.group(2).split(",")) {
                            String name = iface.trim().replace("\"", "");
                            int generic = name.indexOf('<');
                            added.add(generic < 0 ? name : name.substring(0, generic));
                        }
                        CLASSES.put(target.name(), new Info(target.name(), target.superName(), added.toArray(String[]::new), target.isInterface(), target.isAbstract(),
                              target.methods()));
                    }
                }
            }
        }
        Map<String, Set<String>> problems = new TreeMap<>();
        Set<String> unknown = new TreeSet<>();
        for (String className : modClasses) {
            Info info = get(className);
            if (info == null || info.isInterface() || info.isAbstract()) {
                continue;
            }
            //All interfaces (transitively) and the class chain
            List<Info> chain = new ArrayList<>();
            Set<String> interfaces = new HashSet<>();
            ArrayDeque<String> queue = new ArrayDeque<>();
            for (Info c = info; c != null; c = get(c.superName())) {
                chain.add(c);
                queue.addAll(List.of(c.interfaces()));
                if (c.superName() != null && get(c.superName()) == null) {
                    unknown.add(c.superName());
                }
            }
            while (!queue.isEmpty()) {
                String iface = queue.poll();
                if (interfaces.add(iface)) {
                    Info i = get(iface);
                    if (i == null) {
                        unknown.add(iface);
                    } else {
                        queue.addAll(List.of(i.interfaces()));
                    }
                }
            }
            for (String iface : interfaces) {
                Info i = get(iface);
                if (i == null) {
                    continue;
                }
                for (Map.Entry<String, Integer> method : i.methods().entrySet()) {
                    int access = method.getValue();
                    if ((access & Opcodes.ACC_ABSTRACT) == 0 || (access & Opcodes.ACC_STATIC) != 0) {
                        continue;
                    }
                    String key = method.getKey();
                    boolean implemented = chain.stream().anyMatch(c -> {
                        Integer a = c.methods().get(key);
                        return a != null && (a & Opcodes.ACC_ABSTRACT) == 0 && (a & Opcodes.ACC_STATIC) == 0;
                    });
                    if (!implemented) {
                        implemented = interfaces.stream().map(ImplChecker::get).anyMatch(o -> {
                            if (o == null) {
                                return false;
                            }
                            Integer a = o.methods().get(key);
                            return a != null && (a & Opcodes.ACC_ABSTRACT) == 0 && (a & Opcodes.ACC_STATIC) == 0;
                        });
                    }
                    if (!implemented) {
                        problems.computeIfAbsent(iface + "." + key, k -> new TreeSet<>()).add(className);
                    }
                }
            }
        }
        //Every method reference made by mod code must resolve (in the owner, its superclasses or its interfaces) when the owner is known
        int unresolvedReferences = 0;
        for (Map.Entry<String, String> reference : REFERENCES.entrySet()) {
            String key = reference.getKey();
            int dot = key.indexOf('.');
            String owner = key.substring(0, dot);
            String member = key.substring(dot + 1);
            //Signature polymorphic methods resolve against any descriptor
            if (get(owner) == null || member.startsWith("<") || owner.equals("java/lang/invoke/MethodHandle") || owner.equals("java/lang/invoke/VarHandle")) {
                continue;
            }
            if (!resolves(owner, member, new HashSet<>())) {
                problems.computeIfAbsent("unresolved reference " + key, k -> new TreeSet<>()).add(reference.getValue());
                unresolvedReferences++;
            }
        }
        problems.forEach((method, classes) -> System.out.println(method + "  <- " + classes.size() + " classes, e.g. " + classes.iterator().next()));
        System.out.println("TOTAL " + problems.size() + " unimplemented interface methods");
        if (!unknown.isEmpty()) {
            System.out.println("Unresolved hierarchy classes (" + unknown.size() + "): " + unknown.stream().limit(15).toList());
        }
        if (fail && !problems.isEmpty()) {
            System.exit(1);
        }
    }
}
