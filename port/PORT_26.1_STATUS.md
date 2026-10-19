# 26.1.2 port — status (work in progress, does not compile yet)

Last compile: 2008 javac error lines before the ingredient rewrite (`./gradlew compileJava -q > log; python3 scripts/errareas.py log 3`). The ingredient rewrite in progress is not compiled yet; see CLAUDE.md at the repo root.

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

## Next
1. Work the remaining error list: shim common/client (`scripts/api_diff.py` against ref/neo/neoforge.jar), tile code, GUI (`GuiGraphicsExtractor`),
   fabric compat/mixins/bridge, datamaps.
2. Still missing in the shim (see `scripts/shim_refs.py main api`): fluid models/tint sources, GuiLayer, outline renderer events,
   AddClient/ServerReloadListenersEvent, RecipesReceivedEvent, RegisterTextureAtlasesEvent, ClientPacketDistributor, BreakBlockEvent, etc.
3. `neoforge:obj` loader (transmitters, Meka-Tool) and `neoforge:fluid_container` item model (buckets) are not implemented yet.
4. Regenerate the access widener (header only right now; the 1.21.1 one was saved to /tmp/aw_121.accesswidener).
5. Re-apply needed //FABRIC edits from PORT_26.1_REAPPLY.txt, then tests, smoke runs, modules, integrations.
