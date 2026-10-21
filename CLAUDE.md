# Mekanism Fabricated — agent handoff

Unofficial Fabric port of Mekanism (MIT). The 1.21.1 port is finished and released from `main`.
**Current work: porting to Minecraft 26.1.2 on branch `26.1`.** The code under `port/` does not compile yet.

## Rules
- Work on branch `26.1`. Commit in meaningful steps. Push your branch to `origin` so the work isn't lost; never push to `main`, tag, make releases, or publish anywhere else without asking.
- Commit as `-c user.name=Fatin-Ishraq -c user.email=152465554+Fatin-Ishraq@users.noreply.github.com`.
- Don't claim something works unless you built and ran it. Don't try to play test through a GUI; the port is done when it builds, tests pass and the server/client smoke runs start cleanly.
- Keep `port/PORT_26.1_STATUS.md` current (done / next / last error count) before each commit.

## Layout
- `port/` — the Gradle project. Upstream Mekanism 10.8.0 sources (merged from branch `vendor-upstream`) plus Fabric code in `port/src/fabric/`.
- `port/src/fabric/java/mekanism/neoshim/` — **neoshim**, a clean-room reimplementation of the NeoForge API that upstream code compiles against (packages mirror `net.neoforged.*`; the build relocates imports). Prefer fixing things in the shim over editing upstream files. Where upstream files must change, mark the edit with `//FABRIC`.
- Interfaces injected into vanilla classes (NeoForge `I*Extension`) are generated with `port/scripts/gen_injections.py` (Loom interface injection).
- `port/src/fabric/resources/mekanism.accesswidener` — generated from the upstream `accesstransformer.cfg` files (`scripts/at_to_aw.py` + `scripts/resolve_aw_fields.py`), hand additions at the bottom. 26.1 is unobfuscated: the AW uses the `official` namespace.
- `build.gradle` rewrites NeoForge-only JSON in data/assets at build time (conditions → `fabric:load_conditions`, model `loader` → `fabric:type`, NeoForge ingredient types → `fabric:type`).
- `NOTES.md` — the 1.21.1 porting log and architecture decisions. `port/PORT_26.1_REAPPLY.txt` — files whose 1.21.1 `//FABRIC` edits still need re-applying.

## Toolchain
Java 25, Gradle 9.8.1, non-remapping Loom 1.19.1, Fabric Loader 0.20.0, Fabric API 0.155.3+26.1.2. Integrations (JEI, EMI, Jade, CC…) are excluded from the 26.1 build for now.

On a fresh Linux machine run `port/scripts/cloud_setup.sh` (installs JDK 25 to /opt/jdk25, downloads the NeoForge 26.1.2.74 jar to `ref/neo/neoforge.jar`; `ref/` is gitignored), then `export JAVA_HOME=/opt/jdk25 PATH=/opt/jdk25/bin:$PATH`.

## Workflow
```sh
cd port
./gradlew compileJava -q > /tmp/c26.log 2>&1; python3 scripts/errareas.py /tmp/c26.log 3   # errors grouped by area
python3 scripts/errsummary.py /tmp/c26.log symbols|override|files
scripts/errfile.sh /tmp/c26.log <path-substring>                                            # errors for some files
./gradlew -q -I scripts/cp.gradle dumpCp          # once: write the compile classpath to /tmp/mek_cp.txt
scripts/jp.sh -p net.minecraft.world.item.crafting.Ingredient                              # javap MC / Fabric / NeoForge classes
python3 scripts/api_diff.py ../ref/neo/neoforge.jar <shim .java> <NeoForge class>          # shim vs real NeoForge API
python3 scripts/shim_refs.py main api                                                      # NeoForge classes upstream uses that the shim lacks
```
Use the real NeoForge 26.1 jar (`ref/neo/neoforge.jar`, via `jp.sh`) as the API reference for the shim: match class names, method signatures, codec field names and registry ids exactly so upstream code and generated data work unchanged. Decompile MC classes with `./gradlew genSources` or read them with `jp.sh -p -c` when you need behaviour.

## 26.1 API notes gathered so far
- Vanilla `Ingredient` wraps a `HolderSet<Item> values` (AW opens it); codecs `CODEC`, `NON_AIR_HOLDER_SET_CODEC`, `CONTENTS_STREAM_CODEC`; `items()` returns `Stream<Holder<Item>>`, `acceptsItem(Holder)`, `display()`. No `getItems()`, `Value`, `CODEC_NONEMPTY`.
- Fabric custom ingredients: `CustomIngredient` (`test`, `items()`, `requiresTesting`, `getSerializer`, `display`, `toVanilla`), `CustomIngredientSerializer` (`getIdentifier`, `getCodec()`, `getStreamCodec()`), JSON key `fabric:type`.
- NeoForge 26.1 item ingredients: `ICustomIngredient` + `IngredientType(MapCodec, StreamCodec)`; data uses key `neoforge:ingredient_type` (build.gradle must rename it to `fabric:type`). Codec fields: Compound/Intersection `children`, Difference `base`/`subtracted`, DataComponent `items`/`components`/`strict`, SizedIngredient `NESTED_CODEC` `ingredient`/`count`.
- NeoForge 26.1 fluid ingredients: abstract `FluidIngredient` (`CODEC` = dispatch on `neoforge:ingredient_type` xor holder-set `SimpleFluidIngredient`, `fluids()`, `generateFluids()`, `display()`, `isSimple()`, `getType()` → `FluidIngredientType`), `SimpleFluidIngredient(HolderSet<Fluid>)`, `SizedFluidIngredient(FluidIngredient, int amount)`, `CompoundFluidIngredient(children)`, `DataComponentFluidIngredient(HolderSet<Fluid>, DataComponentExactPredicate, boolean strict)`, display package `fluids.crafting.display` (`ForFluidStacks`, `FluidSlotDisplay`, `FluidStackSlotDisplay`, `FluidTagSlotDisplay`, `FluidStackContentsFactory`).
- Renames seen: `GuiGraphics` → `GuiGraphicsExtractor`, `DataComponentPredicate` → `DataComponentExactPredicate`, `ResourceLocation` → `Identifier`, `ItemStackTemplate(Holder<Item>, int, DataComponentPatch)`; `RenderType`, `BlockAndTintGetter`, `FogRenderer`, `LayeredDraw`, `BlockEntityWithoutLevelRenderer`, `MobSpawnType`, `DirectionProperty` are gone or moved — check the jar before guessing.

## Where it stands
See `port/PORT_26.1_STATUS.md` for the done/next list. In progress at handoff: rewriting the neoshim ingredient system for 26.1 (item side mostly done: `IngredientType`, `ICustomIngredient`, Compound/Difference/Intersection/DataComponent; still to do: `SizedIngredient`, `IIngredientExtension`, `fabric/compat/IngredientCodecs` serializer registration, the build.gradle `neoforge:ingredient_type` rename, and the whole `neoshim/neoforge/fluids/crafting` package).
