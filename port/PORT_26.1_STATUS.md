# 26.1.2 port — status (work in progress, does not compile yet)

Last compile: 1420 javac error lines (about 710 errors; the log prints each twice) after the ingredient rewrite (`./gradlew compileJava -q > log; python3 scripts/errareas.py log 3`).
Fixing the shim usually uncovers more errors in later javac phases, so the count is not monotonic.

## Done
- Toolchain: non-remapping Loom 1.19.1, Java 25, Gradle 9.8.1, Fabric API 0.155.3+26.1.2 (core only, integrations excluded).
- Transfer layer: NeoForge 26.1 `ResourceHandler`/`Transaction`/`ItemAccess` shim over the Fabric Transfer API; `Capabilities` + `CapabilityBridge` rewritten.
- Extension interfaces updated to NeoForge 26.1 signatures: `IItemExtension`, `IItemStackExtension`, `IBlockExtension`, `IBlockStateExtension`;
  `setNoCombineRepair` implemented (ItemPropertiesMixin + ItemNoCombineRepairMixin). `FluidStack` implements `FluidInstance`.
- Model system rebuilt for 26.1 (old BakedModel shim and bridge deleted):
  - `DynamicBlockStateModel` renders through Fabric `emitQuads`; `BlockStateModelExtension`, `UnbakedGeometryExtension`, `UnbakedModelExtension`,
    `ResolvedModelExtension`, `ModelBakerExtension`, `BlockStateModelPartExtension`, `BakingResultExtension` injected (scripts/gen_injections.py).
  - `ModelData` moved to `neoforge.model.data`; `BlockGetter.getModelData(pos)` reads Fabric block entity render data.
  - Standalone models -> Fabric extra models; custom block state models -> Fabric `CustomUnbakedBlockStateModel`; model loaders -> Fabric
    `UnbakedModelDeserializer` (build rewrites model JSON `loader` to `fabric:type`); clean-room `neoforge:composite` (fabric/client/model/CompositeModel).
  - Item model / special renderer / select & conditional property events write to the vanilla ID mappers.
  - `ModelEvent.BakingCompleted` fired from ModelManagerApplyMixin; ModelDiscoveryMixin resolves extra dependencies.

- Ingredients rewritten for 26.1: item `IngredientType`/`ICustomIngredient`/Compound/Difference/Intersection/DataComponent/`SizedIngredient`,
  `IIngredientExtension` (isCustom/isSimple/getValues), `fabric/compat/IngredientCodecs` registers them as Fabric custom ingredient serializers under
  `neoforge:*` ids; build.gradle renames `neoforge:ingredient_type` to `fabric:type`. Whole `neoshim/neoforge/fluids/crafting` package rebuilt
  (`FluidIngredient` dispatch codec, Simple/Sized/Compound/DataComponent/Difference/Intersection/CustomDisplay, display package with
  `neoforge:fluid*` slot display types).
- Common shim moved to 26.1 APIs: chunk tickets (`TicketType` registry, codec saved data), `SavedDataStorageMixin` (null DataFixTypes like NeoForge),
  block entity `ValueInput` hooks (+ LevelChunk/ClientPacketListener mixins), FCAP v5, Fabric creative tab API, `WeightedList.Builder` extension,
  `IShearable` injected into `Shearable`, `ResourceKey#identifier`, `Registry#get/getValue`.
- 26.1 phase 1: Curios override is a no-op stub (Trinkets impl is on main), JEI/EMI helpers removed from `IMekanismAccess`, CC/Gender hooks commented
  out in `MekanismHooks`/`ComputerCapabilityHelper`, Fabric compat for JEI/EMI/Trinkets/CC excluded in build.gradle.

- Client shim partly moved to 26.1 (not compiled yet since these edits): GUI layers (`GuiLayer`), `ViewportEvent` fog events on `FogData` +
  FogRendererMixin, `RenderLivingEvent` (render state + SubmitNodeCollector), `EntityRenderersEvent` (AvatarRenderer maps), tint source colour events,
  particle provider registry, `IClientItemExtensions`/`IClientFluidTypeExtensions` 26.1 surfaces, `FluidTintSource(s)`, `RegisterFluidModelsEvent`,
  `FluidModelExtension`, key mapping API rename; obsolete 1.21.1 shim classes removed.

## Next
0. Recompile; still to do in the client shim: `PlaySoundEvent` (`getLocation` -> `getIdentifier`), delete `RegisterShadersEvent` and fix its use in
   `ClientBridge`, then the `mekanism.fabric` glue (ClientBridge, ModBusLifecycle, drivers, mixins) and upstream errors.
1. Work the remaining error list: shim common/client (`scripts/api_diff.py` against ref/neo/neoforge.jar), tile code, GUI (`GuiGraphicsExtractor`),
   fabric compat/mixins/bridge, datamaps.
2. Still missing in the shim (see `scripts/shim_refs.py main api`): fluid models/tint sources, GuiLayer, outline renderer events,
   AddClient/ServerReloadListenersEvent, RecipesReceivedEvent, RegisterTextureAtlasesEvent, ClientPacketDistributor, BreakBlockEvent, etc.
3. `neoforge:obj` loader (transmitters, Meka-Tool) and `neoforge:fluid_container` item model (buckets) are not implemented yet.
4. Regenerate the access widener (header only right now; the 1.21.1 one was saved to /tmp/aw_121.accesswidener).
5. Re-apply needed //FABRIC edits from PORT_26.1_REAPPLY.txt, then tests, smoke runs, modules, integrations.
